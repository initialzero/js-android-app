package com.jaspersoft.android.jaspermobile.activities.file;

import android.accounts.Account;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;

import roboguice.inject.InjectView;
import timber.log.Timber;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_file_open)
public class FileOpenFragment extends RoboSpiceFragment {

    @Inject
    protected JsRestClient jsRestClient;

    @InjectView(R.id.btnTryToOpen)
    protected Button tryToOpen;
    @InjectView(android.R.id.message)
    protected TextView messageView;

    @InstanceState
    @FragmentArg
    protected FileLookup.FileType fileType;

    @InstanceState
    @FragmentArg
    protected String fileUri;

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tryToOpen.setOnClickListener(new TryAgainClickListener());

        tryToOpen();
    }

    private boolean canBeOpened() {
        String mimeType = "application/" + fileType.name();

        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(null, mimeType);
        PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> suitableApps = packageManager.queryIntentActivities(openIntent, PackageManager.GET_INTENT_FILTERS);
        return suitableApps.size() > 0;
    }

    private void tryToOpen() {
        if (canBeOpened()) {
            loadFile();

            showMessageMessage(getString(R.string.fv_can_open_message));
            tryToOpen.setVisibility(View.GONE);
        } else {
            showMessageMessage(getString(R.string.fv_can_not_open_message));
            tryToOpen.setVisibility(View.VISIBLE);
        }
    }

    private void loadFile() {
        File tempResource = getTempFile(fileUri);
        if (tempResource == null) {
            showMessageMessage(getString(R.string.fv_error_message));
            return;
        }

        if (!tempResource.exists()) {
            GetFileContentRequest fileContentRequest = new GetFileContentRequest(jsRestClient, tempResource, fileUri);
            getSpiceManager().execute(fileContentRequest, new FileContentListener());
            showProgressDialog();
        } else {
            openFile(tempResource);
        }
    }

    private void openFile(File file) {
        String mimeType = "application/" + fileType.name();
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(Uri.fromFile(file), mimeType);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getActivity().startActivity(openIntent);
            getActivity().finish();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), getString(R.string.fv_can_not_open_message), Toast.LENGTH_LONG).show();
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

    private void showMessageMessage(String message) {
        messageView.setText(message);
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

    private class TryAgainClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            tryToOpen();
        }
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
        }

        @Override
        public void onRequestSuccess(File file) {
            ProgressDialogFragment.dismiss(getActivity().getSupportFragmentManager());
            openFile(file);
        }
    }
}
