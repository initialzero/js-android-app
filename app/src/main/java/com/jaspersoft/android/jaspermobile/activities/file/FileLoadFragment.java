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
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetFileContentRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.FileLookup;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;

import java.io.File;
import java.util.Date;

import timber.log.Timber;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment
public abstract class FileLoadFragment extends RoboSpiceFragment {

    private static final String TEMP_FILE_NAME = "tempFile";

    @Inject
    protected JsRestClient jsRestClient;

    @Bean
    DefaultPrefHelper prefHelper;

    @InstanceState
    @FragmentArg
    protected FileLookup.FileType fileType;

    @InstanceState
    @FragmentArg
    protected String fileUri;

    protected abstract void onFileReady(File file);

    protected abstract void showErrorMessage();

    protected void loadFile() {
        File resourceFile = getResourceFile();

        if (resourceFile != null) {
            loadFile(resourceFile);
        } else {
            showErrorMessage();
        }
    }

    private void loadFile(File resourceFile) {
        if (!resourceFile.exists() || !isFileValid(resourceFile)) {
            requestFile(resourceFile);
        } else {
            new FileContentListener().onRequestSuccess(resourceFile);
        }
    }

    private void requestFile(File resourceFile) {
        GetFileContentRequest fileContentRequest = new GetFileContentRequest(jsRestClient, resourceFile, fileUri);
        getSpiceManager().execute(fileContentRequest, new FileContentListener());
        showProgressDialog();
    }

    private File getResourceFile(){
        boolean cacheEnabled = isCachingEnabled();
        if (cacheEnabled) {
            return getCacheFile(fileUri);
        } else {
            return getTempFile();
        }
    }

    private File getCacheFile(String resourceUri) {
        File cacheDir = getActivity().getExternalCacheDir();
        File resourceCacheDir = new File(cacheDir, JasperMobileApplication.RESOURCES_CACHE_DIR_NAME);

        Account account = JasperAccountManager.get(getActivity()).getActiveAccount();
        if (account != null) {
            File accountCacheDir = new File(resourceCacheDir, account.name);
            if (!accountCacheDir.exists() && !accountCacheDir.mkdirs()) {
                Timber.e("Unable to create %s", accountCacheDir);
                return null;
            }
            return new File(accountCacheDir, resourceUri);
        }
        return null;
    }

    private File getTempFile() {
        File cacheDir = getActivity().getExternalCacheDir();
        File resourceCacheDir = new File(cacheDir, JasperMobileApplication.RESOURCES_CACHE_DIR_NAME);
        return new File(resourceCacheDir, TEMP_FILE_NAME);
    }

    private boolean isCachingEnabled() {
        return prefHelper.getRepoCacheExpirationValue() != -1;
    }

    private boolean isFileValid(File cacheFile) {
        boolean cacheEnabled = isCachingEnabled();
        if (cacheEnabled) {
            long currentDate = new Date().getTime();
            long cacheExpiration = prefHelper.getRepoCacheExpirationValue();
            long lastModifiedDate = cacheFile.lastModified();
            if (lastModifiedDate + cacheExpiration >= currentDate) return true;
        }
        return false;
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
