package com.jaspersoft.android.jaspermobile.activities.file;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.EFragment;
import org.apache.commons.io.FileUtils;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.client.RestClientException;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.security.auth.login.LoginException;

import roboguice.inject.InjectView;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_html_open)
public class HtmlFileViewFragment extends FileLoadFragment {

    @InjectView(R.id.resourceView)
    protected WebView resourceView;
    @InjectView(R.id.error_text)
    protected TextView errorText;

    @Inject
    protected JsRestClient jsRestClient;

    private Subscription mCookieSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ProgressDialogFragment.builder(getFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        getActivity().finish();
                    }
                }).show();

        mCookieSubscription = CookieManagerFactory.syncCookies(getActivity()).subscribe(new Subscriber<Void>() {
            @Override
            public void onCompleted() {
                loadFile();
            }

            @Override
            public void onError(Throwable e) {
                showErrorMessage();
            }

            @Override
            public void onNext(Void aVoid) {

            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCookieSubscription.unsubscribe();
    }

    @Override
    protected void onFileReady(File file) {
        showHtml(file);
    }

    @Override
    protected void showErrorMessage() {
        errorText.setVisibility(View.VISIBLE);
    }

    private void showHtml(File file) {
        if (file == null) {
            showErrorMessage();
            return;
        }

        String resourceData;
        try {
            resourceData = FileUtils.readFileToString(file);
        } catch (IOException e) {
            showErrorMessage();
            return;
        }
        resourceView.getSettings().setUseWideViewPort(true);
        resourceView.getSettings().setLoadWithOverviewMode(true);
        resourceView.setWebViewClient(new SessionResumeWebViewClient());

        resourceView.loadDataWithBaseURL(getBaseUrl(), resourceData, null, "UTF-8", null);
    }

    private String getBaseUrl() {
        return jsRestClient.getServerProfile().getServerUrl() + "/fileview/fileview" + fileUri.subSequence(0, (fileUri.lastIndexOf('/') + 1));
    }

    private String getAttachmentUrl(String fullResourceUri) {
        return fullResourceUri.replace(jsRestClient.getServerProfile().getServerUrl() + "/fileview/fileview", "");
    }

    private void cacheAttachment(File file, byte[] attachment) {
        try {
            File parentFolder = file.getParentFile();
            if (parentFolder.exists() || parentFolder.mkdirs()) {
                FileCopyUtils.copy(attachment, file);
            }
        } catch (IOException e) {
            // do nothing if attachment can not be cached
        }
    }

    private InputStream getCachedAttachment(File attachmentFile) {
        if (attachmentFile.exists() && isFileValid(attachmentFile)) {
            try {
                return new FileInputStream(attachmentFile);
            } catch (FileNotFoundException e) {
                return null;
            }
        }
        return null;
    }

    private WebResourceResponse fetchAttachment(WebResourceRequest request) throws LoginException, RestClientException {
        File attachmentFile = getResourceFile(getAttachmentUrl(request.getUrl().toString()));
        InputStream attachmentInputStream = getCachedAttachment(attachmentFile);

        if (attachmentInputStream == null) {
            String attachmentUri = request.getUrl().toString();
            byte[] attachmentBinary = jsRestClient.getResourceBinaryData(attachmentUri);
            cacheAttachment(attachmentFile, attachmentBinary);
            attachmentInputStream = new ByteArrayInputStream(attachmentBinary);
        }

        return new WebResourceResponse("image/*", "UTF-8", attachmentInputStream);
    }

    private void reLogin() throws JasperAccountManager.TokenException {
        JasperAccountManager jasperAccountManager = JasperAccountManager.get(getActivity());
        jasperAccountManager.invalidateActiveToken();
        jasperAccountManager.getActiveAuthToken();
    }

    private class SessionResumeWebViewClient extends WebViewClient {

        private Exception mException;

        @Override
        public WebResourceResponse shouldInterceptRequest(WebView view, WebResourceRequest request) {
            WebResourceResponse webResourceResponse = null;
            try {
                webResourceResponse = fetchAttachment(request);
            } catch (LoginException ex) {
                try {
                    reLogin();
                    webResourceResponse = fetchAttachment(request);
                } catch (LoginException exception) {
                    return webResourceResponse;
                } catch (JasperAccountManager.TokenException e) {
                    mException = e;
                }
            } catch (RestClientException ex) {
                mException = ex;
            }
            return webResourceResponse;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);

            ProgressDialogFragment.dismiss(getFragmentManager());

            if (mException != null) {
                RequestExceptionHandler.handle(mException, getActivity());
                showErrorMessage();
            }
        }
    }
}
