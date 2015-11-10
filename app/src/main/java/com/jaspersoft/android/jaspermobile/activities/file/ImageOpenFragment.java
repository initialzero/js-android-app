package com.jaspersoft.android.jaspermobile.activities.file;

import android.accounts.Account;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetFileContentRequest;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;

import java.io.File;

import roboguice.inject.InjectView;
import timber.log.Timber;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_image_open)
public class ImageOpenFragment extends RoboSpiceFragment {

    @Inject
    protected JsRestClient jsRestClient;

    @InjectView(R.id.resourceImage)
    protected ImageView resourceImage;
    @InjectView(R.id.error_text)
    protected TextView errorText;

    @InstanceState
    @FragmentArg
    protected String imageUri;

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        loadFile();
    }

    private void loadFile() {
        File tempResource = getTempFile(imageUri);
        if (tempResource == null) {
            showError();
            return;
        }

        if (!tempResource.exists()) {
            GetFileContentRequest fileContentRequest = new GetFileContentRequest(jsRestClient, tempResource, imageUri);
            getSpiceManager().execute(fileContentRequest, new ImageContentListener());
            showProgressDialog();
            return;
        }
        showImage(tempResource);
    }

    private void showImage(File file) {
        if (file == null) {
            showError();
            return;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        resourceImage.setImageBitmap(bitmap);
    }

    private void showError(){
        errorText.setVisibility(View.VISIBLE);
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

                            }
                        }
                )
                .show();
    }

    private class ImageContentListener extends SimpleRequestListener<File> {
        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            super.onRequestFailure(spiceException);
            ProgressDialogFragment.dismiss(getActivity().getSupportFragmentManager());
            showError();
        }

        @Override
        public void onRequestSuccess(File file) {
            ProgressDialogFragment.dismiss(getActivity().getSupportFragmentManager());
            showImage(file);
        }
    }
}
