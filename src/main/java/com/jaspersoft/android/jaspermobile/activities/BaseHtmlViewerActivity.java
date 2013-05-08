package com.jaspersoft.android.jaspermobile.activities;

import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.SystemClock;
import android.view.View;
import android.webkit.*;
import android.widget.ProgressBar;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.JsRestClient;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.4
 */
public abstract class BaseHtmlViewerActivity extends RoboActivity {

    // Extras
    public static final String EXTRA_RESOURCE_URL = "BaseHtmlViewerActivity.EXTRA_RESOURCE_URL";

    public static final String HTTP_SESSION_ID_NAME = "JSESSIONID";

    @InjectView(R.id.htmlViewer_webView)                protected WebView webView;
    @InjectView(R.id.htmlViewer_webView_progressBar)    protected ProgressBar progressBar;

    @Inject
    protected JsRestClient jsRestClient;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.html_viewer_layout);

        prepareWebView();
        setWebViewClient();
        setCookiesFromRestClient();

        //get resource url from the intent extras
        String url = getIntent().getExtras().getString(EXTRA_RESOURCE_URL);

        // load the report file from the cache folder
        webView.loadUrl(url);
    }

    protected void prepareWebView() {
        webView.getSettings().setPluginsEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setRenderPriority(WebSettings.RenderPriority.HIGH);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setBuiltInZoomControls(true);
    }

    protected void setWebViewClient() {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                // hide progress bar after page load
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    protected void setCookiesFromRestClient() {
        // workaround for http://bugzilla.jaspersoft.com/show_bug.cgi?id=32293
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD){
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
            StrictMode.setThreadPolicy(policy);
        }
        jsRestClient.getResource("/");

        HttpComponentsClientHttpRequestFactory requestFactory = (HttpComponentsClientHttpRequestFactory) jsRestClient.getRestTemplate().getRequestFactory();
        DefaultHttpClient httpClient = (DefaultHttpClient) requestFactory.getHttpClient();

        Cookie sessionCookie = null;
        for(Cookie cookie : httpClient.getCookieStore().getCookies()) {
            if(cookie.getName().equalsIgnoreCase(HTTP_SESSION_ID_NAME)) {
                sessionCookie = cookie;
            }
        }

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        if (sessionCookie != null) {
            cookieManager.removeSessionCookie();
            SystemClock.sleep(500); // yep, it's a hack...
            String cookieString = sessionCookie.getName() + "=" + sessionCookie.getValue() + "; domain=" + sessionCookie.getDomain();
            cookieManager.setCookie(sessionCookie.getDomain(), cookieString);
            CookieSyncManager.getInstance().sync();
        }
    }

}
