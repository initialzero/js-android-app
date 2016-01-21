package com.jaspersoft.android.jaspermobile.activities.file;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetFileResourceRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.FileLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EActivity(R.layout.activity_file_viewer)
public class FileViewerActivity extends RoboSpiceActivity {

    @Extra
    protected ResourceLookup resourceLookup;

    @Inject
    protected JsRestClient jsRestClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        showFileTitle();
        if (savedInstanceState == null) {
            getFileInfo();
            showProgressDialog();
        }
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_fv_s);
    }

    private void showProgressDialog() {
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        }
                )
                .show();
    }

    private void showFileTitle() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(resourceLookup.getLabel());
        }
    }

    private void getFileInfo() {
        GetFileResourceRequest fileResourceRequest = new GetFileResourceRequest(jsRestClient, resourceLookup.getUri());
        long cacheExpiryDuration = DefaultPrefHelper_.getInstance_(this).getRepoCacheExpirationValue();
        getSpiceManager().execute(fileResourceRequest, fileResourceRequest.createCacheKey(), cacheExpiryDuration, new FileInfoListener());
    }

    private class FileInfoListener extends SimpleRequestListener<FileLookup> {
        @Override
        protected Context getContext() {
            return FileViewerActivity.this;
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            super.onRequestFailure(spiceException);
            finish();
        }

        @Override
        public void onRequestSuccess(FileLookup fileLookup) {
            ProgressDialogFragment.dismiss(getSupportFragmentManager());
            Fragment fileFragment;
            switch (fileLookup.getFileType()) {
                case html:
                    fileFragment = HtmlFileViewFragment_.builder()
                            .fileType(fileLookup.getFileType())
                            .fileUri(fileLookup.getUri())
                            .build();
                    break;
                case img:
                    fileFragment = ImageFileViewFragment_.builder()
                            .fileType(fileLookup.getFileType())
                            .fileUri(fileLookup.getUri())
                            .build();
                    break;
                case pdf:
                case xls:
                case xlsx:
                case docx:
                case ods:
                case odt:
                case pptx:
                case rtf:
                case csv:
                    fileFragment = ExternalOpenFragment_.builder()
                            .fileType(fileLookup.getFileType())
                            .fileUri(fileLookup.getUri())
                            .build();
                    break;
                default:
                    fileFragment = UnsupportedFilesFragment_.builder()
                            .fileType(fileLookup.getFileType())
                            .fileUri(fileLookup.getUri())
                            .build();
                    break;
            }
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_frame, fileFragment)
                    .commit();

            analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.VIEWED.getValue(), fileLookup.getFileType().name());
        }
    }
}
