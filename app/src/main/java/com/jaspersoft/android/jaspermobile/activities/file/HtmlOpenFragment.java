package com.jaspersoft.android.jaspermobile.activities.file;

import android.accounts.Account;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetFileContentRequest;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;
import timber.log.Timber;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_html_open)
public class HtmlOpenFragment extends RoboSpiceFragment {

    @Inject
    protected JsRestClient jsRestClient;

    @InjectView(R.id.resourceView)
    protected WebView resourceView;
    @InjectView(R.id.error_text)
    protected TextView errorText;

    @InstanceState
    @FragmentArg
    protected String htmlUri;

    private Subscription mCookieSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCookieSubscription = CookieManagerFactory.syncCookies(getActivity()).subscribe(
                new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        loadFile();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showError();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCookieSubscription.unsubscribe();
    }

    private void loadFile() {
        File tempResource = getTempFile(htmlUri);
        if (tempResource == null) {
            showError();
            return;
        }

        if (!tempResource.exists()) {
            GetFileContentRequest fileContentRequest = new GetFileContentRequest(jsRestClient, tempResource, htmlUri);
            getSpiceManager().execute(fileContentRequest, new ImageContentListener());
            showProgressDialog();
            return;
        }
        showHtml(tempResource);
    }

    private void showHtml(File file) {
        if (file == null) {
            showError();
            return;
        }

        String resourceData;
        try {
            resourceData = FileUtils.readFileToString(file);
        } catch (IOException e) {
            showError();
            return;
        }
        resourceView.getSettings().setUseWideViewPort(true);
        resourceView.getSettings().setLoadWithOverviewMode(true);
        resourceView.loadDataWithBaseURL(getBaseUrl(), resourceData, null, "UTF-8", null);
    }

    private String getBaseUrl(){
        return jsRestClient.getServerProfile().getServerUrl() + "/fileview/fileview" + htmlUri.subSequence(0, (htmlUri.lastIndexOf('/') + 1));
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
                                getActivity().finish();
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
            showHtml(file);
        }
    }
}
