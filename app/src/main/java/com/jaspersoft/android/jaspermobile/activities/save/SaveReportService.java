package com.jaspersoft.android.jaspermobile.activities.save;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.OperationCanceledException;
import android.support.v4.app.NotificationCompat;
import android.support.v4.os.AsyncTaskCompat;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.data.CancelExportBundle;
import com.jaspersoft.android.jaspermobile.data.ExportBundle;
import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.cache.report.ExportOperationCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.db.model.SavedItems;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.util.SavedItemHelper;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.data.report.PageRange;
import com.jaspersoft.android.sdk.service.data.report.ReportExportOutput;
import com.jaspersoft.android.sdk.service.data.report.ResourceOutput;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.report.ReportAttachment;
import com.jaspersoft.android.sdk.service.report.ReportExecution;
import com.jaspersoft.android.sdk.service.report.ReportExecutionOptions;
import com.jaspersoft.android.sdk.service.report.ReportExport;
import com.jaspersoft.android.sdk.service.report.ReportExportOptions;
import com.jaspersoft.android.sdk.service.report.ReportFormat;
import com.jaspersoft.android.sdk.service.report.ReportService;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EIntentService;
import org.androidannotations.annotations.ServiceAction;
import org.androidannotations.annotations.SystemService;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import javax.inject.Inject;

import timber.log.Timber;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */

@EIntentService
public class SaveReportService extends IntentService {

    private static final int LOADING_NOTIFICATION_ID = 434;
    public static final String TAG = SaveReportService.class.getSimpleName();

    @Bean
    protected SavedItemHelper savedItemHelper;

    @Inject
    protected JasperRestClient mRestClient;
    @Inject
    protected ReportParamsCache mReportParamsCache;
    @Inject
    protected ReportParamsMapper mReportParamsMapper;
    @Inject
    protected Profile mProfile;
    @Inject
    protected PreExecutionThread mPreExecutionThread;
    @Inject
    protected PostExecutionThread mPostExecutionThread;
    @Inject
    protected ExportOperationCache mExportOperationCache;

    @Inject
    protected Analytics analytics;

    @SystemService
    NotificationManager mNotificationManager;

    private final Queue<Uri> mRecordUrisQe;
    private final List<Uri> mRecordsToDel;
    private Uri mCurrent;

    public SaveReportService() {
        super(TAG);
        mRecordUrisQe = new LinkedList<>();
        mRecordsToDel = new ArrayList<>();
    }

    public static void start(Context context, ExportBundle bundle) {
        SaveReportService_
                .intent(context)
                .saveReport(bundle)
                .start();
    }

    public static void cancel(Context context, CancelExportBundle bundle) {
        SaveReportService_
                .intent(context)
                .cancelSaving(bundle.getEntityId(), bundle.getFile())
                .start();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        GraphObject.Factory.from(this)
                .getProfileComponent()
                .inject(this);

        String action = intent.getAction();
        if (SaveReportService_.ACTION_CANCEL_SAVING.equals(action)) {
            cancelSavingReport(intent.getExtras());
            return super.onStartCommand(intent, flags, startId);
        }

        Bundle extras = intent.getExtras();
        ExportBundle bundle = extras.getParcelable(SaveReportService_.EXPORT_BUNDLE_EXTRA);
        Uri savedItemRecord = addSavedItemRecord(bundle);
        addRecord(savedItemRecord);

        notifyDownloadingCount();

        return super.onStartCommand(intent, flags, startId);
    }

    private void addRecord(Uri savedItemRecord) {
        mRecordUrisQe.add(savedItemRecord);
    }

    @Override
    protected void onHandleIntent(Intent saveReportIntent) {
    }

    @ServiceAction
    protected void saveReport(final ExportBundle exportBundle) {
        mCurrent = mRecordUrisQe.peek();
        if (mRecordsToDel.contains(mCurrent)) {
            mCurrent = null;
            removeRecord();
            mRecordsToDel.remove(mCurrent);
            return;
        }

        AsyncTask<ExportBundle, Void, Void> operation = exportReport(exportBundle);
        mExportOperationCache.add(mCurrent, operation);
    }

    private void removeRecord() {
        mRecordUrisQe.poll();
    }

    private AsyncTask<ExportBundle, Void, Void> exportReport(final ExportBundle bundle) {
        return AsyncTaskCompat.executeParallel(new AsyncTask<ExportBundle, Void, Void>() {
            @Override
            protected Void doInBackground(ExportBundle... params) {
                ExportBundle bundle = params[0];
                syncSaveAction(bundle);
                return null;
            }
        }, bundle);
    }

    private void syncSaveAction(ExportBundle exportBundle) {
        File reportFile = exportBundle.getFile();
        String savedReportName = exportBundle.getLabel();

        if (moreThanOneActiveExports()) {
            notifyDownloadingCount();
        } else {
            notifyDownloadingName(savedReportName);
        }

        try {
            startExport(exportBundle);
            updateSavedItemRecordToDownloaded(mRecordUrisQe.peek());

            notifySaveResult(savedReportName, android.R.drawable.stat_sys_download_done, getString(R.string.sr_t_report_saved));
            analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.SAVED.getValue(), Analytics.EventLabel.DONE.getValue());
        } catch (ServiceException | IOException ex) {
            notifySaveResult(savedReportName, android.R.drawable.ic_dialog_alert, getString(R.string.sdr_saving_error_msg));
            analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.SAVED.getValue(), Analytics.EventLabel.FAILED.getValue());
            savedItemHelper.deleteSavedItem(reportFile, mRecordUrisQe.peek());
        } catch (OperationCanceledException ex) {
            savedItemHelper.deleteSavedItem(reportFile, mRecordUrisQe.peek());
        } finally {
            removeRecord();
            if (atLeastOneExportActive()) {
                notifyDownloadingCount();
            } else {
                cancelDownloadingNotification();
            }
        }
    }

    private void startExport(ExportBundle bundle) throws ServiceException, IOException {
        String reportUri = bundle.getUri();
        String format = bundle.getFormat();
        String range = bundle.getPageRange();
        File file = bundle.getFile();

        ReportFormat reportFormat = ReportFormat.valueOf(format);
        PageRange pageRange = PageRange.parse(range);

        List<ReportParameter> parameters = mReportParamsCache.get(reportUri);
        List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> params =
                mReportParamsMapper.legacyParamsToRetrofitted(parameters);

        ReportService reportService = mRestClient.syncReportService();
        ReportExecutionOptions execOptions = ReportExecutionOptions.builder()
                .withInteractive(false)
                .withFormat(reportFormat)
                .withAttachmentPrefix("./")
                .withPageRange(pageRange)
                .withParams(params)
                .build();
        ReportExecution run = reportService.run(reportUri, execOptions);

        ReportExportOptions exportOptions = ReportExportOptions.builder()
                .withAttachmentPrefix("./")
                .withFormat(reportFormat)
                .withPageRange(pageRange)
                .build();
        ReportExport export = run.export(exportOptions);
        ReportExportOutput output = export.download();

        FileUtils.copyInputStreamToFile(output.getStream(), file);
        if ("HTML".equals(format)) {
            List<ReportAttachment> attachments = export.getAttachments();

            for (ReportAttachment attachment : attachments) {
                File attachmentFile = new File(file.getParentFile(), attachment.getFileName());
                ResourceOutput resourceOutput = attachment.download();
                FileUtils.copyInputStreamToFile(resourceOutput.getStream(), attachmentFile);
            }
        }
    }

    @ServiceAction
    protected void cancelSaving(String itemUri, File reportFile) {
    }

    private PendingIntent getSavedItemIntent() {
        Intent notificationIntent = NavigationActivity_.intent(this)
                .currentSelection(R.id.vg_saved_items)
                .get();

        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private int createNotificationId() {
        long time = new Date().getTime();
        String tmpStr = String.valueOf(time);
        String last5Str = tmpStr.substring(tmpStr.length() - 6);
        return Integer.valueOf(last5Str);
    }

    private Uri addSavedItemRecord(ExportBundle bundle) {
        String descriptionExtra = bundle.getDescription();
        String outputFormatExtra = bundle.getFormat();
        File reportFileExtra = bundle.getFile();
        String savedReportNameExtra = bundle.getLabel();

        SavedItems savedItemsEntry = new SavedItems();
        savedItemsEntry.setName(savedReportNameExtra);
        savedItemsEntry.setFilePath(reportFileExtra.getPath());
        savedItemsEntry.setFileFormat(outputFormatExtra);
        savedItemsEntry.setDescription(descriptionExtra);
        savedItemsEntry.setWstype(ResourceLookup.ResourceType.reportUnit.toString());
        savedItemsEntry.setCreationTime(new Date().getTime());
        savedItemsEntry.setAccountName(mProfile.getKey());
        savedItemsEntry.setDownloaded(false);

        return getContentResolver().insert(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                savedItemsEntry.getContentValues());
    }

    private void cancelSavingReport(Bundle reportBundle) {
        Uri reportUri = Uri.parse(reportBundle.getString(SaveReportService_.ITEM_URI_EXTRA));
        AsyncTask operation = mExportOperationCache.get(reportUri);

        if (operation != null) {
            boolean canceled = operation.cancel(true);
            if (!canceled) {
                Timber.e("Operation not cancelled");
                while (!operation.cancel(true)) {
                    Timber.e("Trying to cancell");
                }
            }
            mExportOperationCache.remove(reportUri);

            if (reportUri.equals(mCurrent)) {
                mCurrent = null;
                return;
            }

            if (mRecordsToDel.add(reportUri)) {
                File reportFile = ((File) reportBundle.getSerializable(SaveReportService_.REPORT_FILE_EXTRA));
                savedItemHelper.deleteSavedItem(reportFile, reportUri);
            }

            analytics.sendEvent(
                    Analytics.EventCategory.RESOURCE.getValue(),
                    Analytics.EventAction.SAVED.getValue(),
                    Analytics.EventLabel.CANCELED.getValue()
            );
        }
    }

    private boolean updateSavedItemRecordToDownloaded(Uri recordUri) {
        if (mCurrent == null)
            throw new OperationCanceledException("Saving canceled!");

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
                .setContentTitle(getString(R.string.sdr_saving_multiply_msg, getPendingCount()))
                .setContentIntent(getSavedItemIntent());

        mNotificationManager.notify(LOADING_NOTIFICATION_ID, mBuilder.build());
    }

    private boolean moreThanOneActiveExports() {
        return getPendingCount() > 1;
    }

    private boolean atLeastOneExportActive() {
        return getPendingCount() > 0;
    }

    private int getPendingCount() {
        return mRecordUrisQe.size() - mRecordsToDel.size();
    }

    private void cancelDownloadingNotification() {
        mNotificationManager.cancel(LOADING_NOTIFICATION_ID);
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
