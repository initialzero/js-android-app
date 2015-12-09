package com.jaspersoft.android.jaspermobile.activities.save;

import android.accounts.Account;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.activities.save.fragment.SaveItemFragment;
import com.jaspersoft.android.jaspermobile.db.model.SavedItems;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.jaspermobile.util.SavedItemHelper;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.ReportAttachment;
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ExportsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportOutputResource;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.report.ReportStatus;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.SystemService;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import roboguice.service.RoboIntentService;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */

@EIntentService
public class SaveReportService extends RoboIntentService {

    private static final int LOADING_NOTIFICATION_ID = 434;
    public static final String TAG = SaveReportService.class.getSimpleName();

    @Bean
    protected SavedItemHelper savedItemHelper;

    @Inject
    protected JsRestClient jsRestClient;

    @SystemService
    NotificationManager mNotificationManager;

    private Queue<Uri> mRecordUrisQe;

    public SaveReportService() {
        super(TAG);
        mRecordUrisQe = new LinkedList<>();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Uri savedItemRecord = addSavedItemRecord(intent.getExtras());
        mRecordUrisQe.add(savedItemRecord);

        notifyDownloadingCount();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onHandleIntent(Intent saveReportIntent) {
    }

    @ServiceAction
    protected void saveReport(String savedReportName, String reportDescription, SaveItemFragment.OutputFormat outputFormat, File reportFile, String pageRange, String requestId) {

        notifyDownloadingName(savedReportName);

        Uri itemUri = mRecordUrisQe.peek();
        try {
            waitForExecutionBegin(requestId);

            ExportExecution export;
            try {
                export = exportReport(requestId, outputFormat, pageRange);
            } catch (RestClientException | IllegalStateException ex) {
                export = exportReport(requestId, outputFormat, pageRange);
            }

            saveReport(reportFile, export.getId(), requestId);
            if (SaveItemFragment.OutputFormat.HTML == outputFormat) {
                saveAttachments(reportFile, export.getId(), requestId);
            }
            updateSavedItemRecordToDownloaded(mRecordUrisQe.peek());

            notifySaveResult(savedReportName, android.R.drawable.stat_sys_download_done, getString(R.string.sr_t_report_saved));
        } catch (RestClientException | IllegalStateException ex) {
            notifySaveResult(savedReportName, android.R.drawable.ic_dialog_alert, getString(R.string.sdr_saving_error_msg));
            savedItemHelper.deleteSavedItem(reportFile, itemUri);
        } finally {
            mRecordUrisQe.poll();
            notifyDownloadingCount();
        }
    }

    private ExportExecution exportReport(String requestId, SaveItemFragment.OutputFormat outputFormat, String pageRange) {
        ExportsRequest executionData = createReportExportRequest(outputFormat, pageRange);
        ExportExecution export = jsRestClient.runExportForReport(requestId, executionData);
        waitForExportDone(requestId, export.getId());
        return export;
    }

    private PendingIntent getSavedItemIntent() {
        Intent notificationIntent = NavigationActivity_.intent(this)
                .currentSelection(R.id.vg_saved_items)
                .get();

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private ExportsRequest createReportExportRequest(SaveItemFragment.OutputFormat outputFormat, String pageRange) {
        ExportsRequest exportsRequest = new ExportsRequest();
        exportsRequest.setOutputFormat(outputFormat.toString());
        exportsRequest.setEscapedAttachmentsPrefix("./");
        exportsRequest.setPages(pageRange);

        return exportsRequest;
    }

    private void waitForExecutionBegin(String reportExecutionId) {
        ReportStatus reportStatus;
        do {
            sleep();
            reportStatus = jsRestClient.runReportStatusCheck(reportExecutionId).getReportStatus();
            if (reportStatus == ReportStatus.failed)
                throw new IllegalStateException("Report execution failed!");
            if (reportStatus == ReportStatus.cancelled)
                throw new IllegalStateException("Report execution canceled!");
        }
        while (reportStatus != ReportStatus.ready && reportStatus != ReportStatus.execution);
    }

    private void waitForExportDone(String reportExecutionId, String exportId) {
        ReportStatus reportStatus;
        do {
            sleep();
            reportStatus = jsRestClient.runExportStatusCheck(reportExecutionId, exportId).getReportStatus();
            if (reportStatus == ReportStatus.failed)
                throw new IllegalStateException("Report export failed!");
            if (reportStatus == ReportStatus.cancelled)
                throw new IllegalStateException("Report export canceled!");
        }
        while (reportStatus != ReportStatus.ready);
    }

    private void sleep() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private int createNotificationId() {
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last5Str = tmpStr.substring(tmpStr.length() - 6);
        return Integer.valueOf(last5Str);
    }

    private void saveReport(File reportFile, String exportOutput, String executionId) throws RestClientException {
        jsRestClient.saveExportOutputToFile(executionId, exportOutput, reportFile);
    }

    private void saveAttachments(File reportFile, String exportId, String executionId) {
        ReportExecutionResponse executionMetadata = jsRestClient.runReportDetailsRequest(executionId);
        if (executionMetadata.getExports().isEmpty()) return;

        List<ReportOutputResource> attachments = null;
        for (ExportExecution exportExecution : executionMetadata.getExports()) {
            if (exportExecution.getId().equals(exportId)) {
                attachments = exportExecution.getAttachments();
            }
        }

        if (attachments == null) return;

        for (ReportOutputResource attachment : attachments) {
            String attachmentName = attachment.getFileName();
            File attachmentFile = new File(reportFile.getParentFile(), attachmentName);

            jsRestClient.saveExportAttachmentToFile(executionId, exportId, attachmentName, attachmentFile);
        }
    }

    private Uri addSavedItemRecord(Bundle reportBundle) {
        Account currentAccount = JasperAccountManager.get(this).getActiveAccount();

        String descriptionExtra = reportBundle.getString(SaveReportService_.REPORT_DESCRIPTION_EXTRA);
        SaveItemFragment.OutputFormat outputFormatExtra = ((SaveItemFragment.OutputFormat) reportBundle.getSerializable(SaveReportService_.OUTPUT_FORMAT_EXTRA));
        File reportFileExtra = ((File) reportBundle.getSerializable(SaveReportService_.REPORT_FILE_EXTRA));
        String savedReportNameExtra = reportBundle.getString(SaveReportService_.SAVED_REPORT_NAME_EXTRA);

        SavedItems savedItemsEntry = new SavedItems();
        savedItemsEntry.setName(savedReportNameExtra);
        savedItemsEntry.setFilePath(reportFileExtra.getPath());
        savedItemsEntry.setFileFormat(outputFormatExtra.toString());
        savedItemsEntry.setDescription(descriptionExtra);
        savedItemsEntry.setWstype(ResourceLookup.ResourceType.reportUnit.toString());
        savedItemsEntry.setCreationTime(new Date().getTime());
        savedItemsEntry.setAccountName(currentAccount.name);
        savedItemsEntry.setDownloaded(false);

        return getContentResolver().insert(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                savedItemsEntry.getContentValues());
    }

    private boolean updateSavedItemRecordToDownloaded(Uri recordUri) {
        SavedItems savedItemsEntry = new SavedItems();
        savedItemsEntry.setDownloaded(true);

        return getContentResolver().update(recordUri, savedItemsEntry.getContentValues(), null, null) == 1;
    }

    private void startForegroundNotification() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentTitle(getString(R.string.sdr_starting_downloading_msg));
        startForeground(LOADING_NOTIFICATION_ID, mBuilder.build());
    }

    private void notifyDownloadingName(String reportName) {
        if (mRecordUrisQe.size() != 1) return;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentText(getString(R.string.sdr_saving_msg))
                .setContentTitle(reportName)
                .setContentIntent(getSavedItemIntent());

        mNotificationManager.notify(LOADING_NOTIFICATION_ID, mBuilder.build());

    }

    private void notifyDownloadingCount() {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentText(getString(R.string.sdr_saving_msg))
                .setContentTitle(getString(R.string.sdr_saving_multiply_msg, mRecordUrisQe.size()))
                .setContentIntent(getSavedItemIntent());

        mNotificationManager.notify(LOADING_NOTIFICATION_ID, mBuilder.build());
    }

    private void notifySaveResult(String reportName, int iconId, String message) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(iconId)
                .setContentTitle(reportName)
                .setContentText(message)
                .setContentIntent(getSavedItemIntent())
                .setAutoCancel(true);

        mNotificationManager.notify(createNotificationId(), mBuilder.build());
    }
}
