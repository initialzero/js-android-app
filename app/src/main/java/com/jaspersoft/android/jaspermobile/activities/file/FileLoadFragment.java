package com.jaspersoft.android.jaspermobile.activities.file;

import android.accounts.Account;
import android.content.Context;
import android.content.DialogInterface;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetFileContentRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.FileLookup;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;

import java.io.File;

import timber.log.Timber;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment
public abstract class FileLoadFragment extends RoboSpiceFragment {

    @Inject
    protected JsRestClient jsRestClient;

    @InstanceState
    @FragmentArg
    protected FileLookup.FileType fileType;

    @InstanceState
    @FragmentArg
    protected String fileUri;

    protected abstract void onFileReady(File file);

    protected abstract void showErrorMessage();

    protected void loadFile() {
        File tempResource = getTempFile(fileUri);
        if (tempResource != null) {
            if (!tempResource.exists()) {
                GetFileContentRequest fileContentRequest = new GetFileContentRequest(jsRestClient, tempResource, fileUri);
                getSpiceManager().execute(fileContentRequest, new FileContentListener());
                showProgressDialog();
            } else {
                new FileContentListener().onRequestSuccess(tempResource);
            }
        }
    }

    private File getTempFile(String resourceUri) {
        File cacheDir = getActivity().getExternalCacheDir();
        File resourceCacheDir = new File(cacheDir, JasperMobileApplication.RESOURCES_CACHE_DIR_NAME);

        Account account = JasperAccountManager.get(getActivity()).getActiveAccount();
        if (account != null) {
            File accountReportDir = new File(resourceCacheDir, account.name);
            if (!accountReportDir.exists() && !accountReportDir.mkdirs()) {
                Timber.e("Unable to create %s", accountReportDir);
                return null;
            }
            return new File(accountReportDir, resourceUri);
        }
       return null;
    }

    private void showProgressDialog() {
        ProgressDialogFragment.builder(getActivity().getSupportFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .setOnCancelListener(
                        new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                getActivity().finish();
                            }
                        }
                )
                .show();
    }

    private class FileContentListener extends SimpleRequestListener<File> {
        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            super.onRequestFailure(spiceException);
            ProgressDialogFragment.dismiss(getActivity().getSupportFragmentManager());
            showErrorMessage();
        }

        @Override
        public void onRequestSuccess(File file) {
            ProgressDialogFragment.dismiss(getActivity().getSupportFragmentManager());
            onFileReady(file);
        }
    }
}
