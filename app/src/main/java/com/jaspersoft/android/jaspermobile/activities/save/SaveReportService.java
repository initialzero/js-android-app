package com.jaspersoft.android.jaspermobile.activities.save;

import android.accounts.Account;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
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
import com.jaspersoft.android.sdk.client.oxm.report.ExportExecution;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;
import com.jaspersoft.android.sdk.client.oxm.report.ReportOutputResource;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.springframework.web.client.RestClientException;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

import roboguice.service.RoboIntentService;
import timber.log.Timber;

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
    @Inject
    protected ReportParamsStorage paramsStorage;

    private int reportToSaveCount;

    public SaveReportService() {
        super(TAG);
        reportToSaveCount = 0;
    }

    @Override
    public void onStart(Intent saveReportIntent, int startId) {
        super.onStart(saveReportIntent, startId);

        String savedReportName = saveReportIntent.getStringExtra(SaveItemFragment.SAVED_REPORT_ARG);

        reportToSaveCount++;
        updateReportsSavingNotification(savedReportName);
    }

    @Override
    protected void onHandleIntent(Intent saveReportIntent) {
        ResourceLookup resourceLookup = saveReportIntent.getParcelableExtra(SaveItemFragment.RESOURCE_LOOKUP_ARG);
        SaveItemFragment.OutputFormat outputFormat = (SaveItemFragment.OutputFormat) saveReportIntent.getSerializableExtra(SaveItemFragment.OUTPUT_FORMAT_ARG);
        File reportFile = new File(saveReportIntent.getStringExtra(SaveItemFragment.REPORT_FILE_ARG));
        int fromPage = saveReportIntent.getIntExtra(SaveItemFragment.FROM_PAGE_ARG, 1);
        int toPage = saveReportIntent.getIntExtra(SaveItemFragment.TO_PAGE_ARG, 1);
        String savedReportName = saveReportIntent.getStringExtra(SaveItemFragment.SAVED_REPORT_ARG);
        List<ReportParameter> reportParameters = getReportParams(resourceLookup.getUri());

        try {
            ReportExecutionRequest runReportExecutionRequest = createReportExecutionRequest(resourceLookup, outputFormat, reportParameters, fromPage, toPage);
            ReportExecutionResponse runReportResponse = jsRestClient.runReportExecution(runReportExecutionRequest);
            ExportExecution execution = runReportResponse.getExports().get(0);

            saveReport(reportFile, outputFormat, execution.getAttachments(), execution.getId(), runReportResponse.getRequestId());

            addSavedItemRecord(reportFile, outputFormat, resourceLookup, savedReportName);
            notifySaveResult(savedReportName, android.R.drawable.stat_sys_download_done, getString(R.string.sr_t_report_saved));
        } catch (RestClientException ex) {
            notifySaveResult(savedReportName, android.R.drawable.ic_dialog_alert, getString(R.string.sdr_saving_error_msg));
            removeIncorrectlySavedFiles(reportFile);
        } finally {
            reportToSaveCount--;
            updateReportsSavingNotification(savedReportName);
        }
    }

    private List<ReportParameter> getReportParams(String reportUri) {
        return paramsStorage.getInputControlHolder(reportUri).getReportParams();
    }

    private PendingIntent getSavedItemIntent() {
        Intent notificationIntent = NavigationActivity_.intent(this)
                .currentSelection(R.id.vg_saved_items)
                .get();

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private ReportExecutionRequest createReportExecutionRequest(ResourceLookup resource, SaveItemFragment.OutputFormat outputFormat,
                                                                List<ReportParameter> reportParameters, int fromPage, int toPage) {

        ReportExecutionRequest executionData = new ReportExecutionRequest();
        executionData.setReportUnitUri(resource.getUri());
        executionData.setInteractive(false);
        executionData.setOutputFormat(outputFormat.toString());
        executionData.setEscapedAttachmentsPrefix("./");

        boolean pagesNumbersIsValid = fromPage > 0 && toPage > 0 && toPage >= fromPage;
        if (pagesNumbersIsValid) {
            boolean isRange = fromPage < toPage;
            if (isRange) {
                executionData.setPages(fromPage + "-" + toPage);
            } else {
                executionData.setPages(String.valueOf(fromPage));
            }
        }

        if (!reportParameters.isEmpty()) {
            executionData.setParameters(reportParameters);
        }

        return executionData;
    }

    private int createNotificationId() {
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last5Str = tmpStr.substring(tmpStr.length() - 6);
        return Integer.valueOf(last5Str);
    }

    private void saveReport(File reportFile, SaveItemFragment.OutputFormat outputFormat, List<ReportOutputResource> attachments, String exportOutput, String executionId) throws RestClientException {
        jsRestClient.saveExportOutputToFile(executionId, exportOutput, reportFile);

        // save attachments
        if (SaveItemFragment.OutputFormat.HTML == outputFormat) {
            for (ReportOutputResource attachment : attachments) {
                String attachmentName = attachment.getFileName();
                File attachmentFile = new File(reportFile.getParentFile(), attachmentName);

                jsRestClient.saveExportAttachmentToFile(executionId, exportOutput, attachmentName, attachmentFile);
            }
        }
    }

    private Uri addSavedItemRecord(File reportFile, SaveItemFragment.OutputFormat fileFormat, ResourceLookup resourceLookup, String savedReportName) {
        Account currentAccount = JasperAccountManager.get(this).getActiveAccount();
        SavedItems savedItemsEntry = new SavedItems();

        savedItemsEntry.setName(savedReportName);
        savedItemsEntry.setFilePath(reportFile.getPath());
        savedItemsEntry.setFileFormat(fileFormat.toString());
        savedItemsEntry.setDescription(resourceLookup.getDescription());
        savedItemsEntry.setWstype(resourceLookup.getResourceType().toString());
        savedItemsEntry.setCreationTime(new Date().getTime());
        savedItemsEntry.setAccountName(currentAccount.name);

        return getContentResolver().insert(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                savedItemsEntry.getContentValues());
    }

    private void updateReportsSavingNotification(String reportName) {
        Notification.Builder mBuilder = new Notification.Builder(this)
                .setSmallIcon(android.R.drawable.stat_sys_download)
                .setContentText(getString(R.string.sdr_saving_msg));

        if (reportToSaveCount == 1) {
            mBuilder.setContentTitle(reportName);
            startForeground(LOADING_NOTIFICATION_ID, mBuilder.build());
        } else {
            mBuilder.setContentTitle(getString(R.string.sdr_saving_multiply_msg, reportToSaveCount));
            NotificationManager mNotifyMgr =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            mNotifyMgr.notify(LOADING_NOTIFICATION_ID, mBuilder.build());
        }
    }

    private void notifySaveResult(String reportName, int iconId, String message) {

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(iconId)
                .setContentTitle(reportName)
                .setContentText(message)
                .setContentIntent(getSavedItemIntent())
                .setAutoCancel(true);

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        mNotifyMgr.notify(createNotificationId(), mBuilder.build());
    }

    private void removeIncorrectlySavedFiles(File reportFile) {
        if (reportFile == null) return;

        File dir = reportFile.getParentFile();
        try {
            org.apache.commons.io.FileUtils.deleteDirectory(dir);
        } catch (IOException e) {
            Timber.w(TAG, "Failed to remove template file", e);
        }
    }
}
