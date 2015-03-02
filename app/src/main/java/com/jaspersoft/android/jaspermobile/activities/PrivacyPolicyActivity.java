package com.jaspersoft.android.jaspermobile.activities;

import android.content.Context;
import android.os.Bundle;
import android.webkit.WebViewClient;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.fragment.WebViewFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.fragment.WebViewFragment_;
import com.jaspersoft.android.jaspermobile.network.PrivacyRequest;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener2;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
@EActivity
public class PrivacyPolicyActivity extends RoboSpiceActivity implements WebViewFragment.OnWebViewCreated {

    private WebViewFragment webViewFragment;

    @Inject
    PrivacyRequest request;

    @Bean
    DefaultPrefHelper prefHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            webViewFragment = WebViewFragment_.builder()
                    .resourceLabel(getString(R.string.sa_about_privacy))
                    .build();

            webViewFragment.setOnWebViewCreated(this);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, webViewFragment, ResourcesControllerFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void onWebViewCreated(final WebViewFragment webViewFragment) {
        webViewFragment.getWebView().setWebViewClient(new WebViewClient());

        getSpiceManager().getFromCacheAndLoadFromNetworkIfExpired(request,
                request.createCacheKey(), prefHelper.getRepoCacheExpirationValue(),
                new PrivacyRequestListener());
    }

    private class PrivacyRequestListener extends SimpleRequestListener2<String> {

        @Override
        protected Context getContext() {
            return PrivacyPolicyActivity.this;
        }

        @Override
        public void onRequestSuccess(String privacy) {
            if (privacy == null) {
                return;
            }
            webViewFragment.loadHtml(PrivacyRequest.PRIVACY_URL, privacy);
        }
    }

}
