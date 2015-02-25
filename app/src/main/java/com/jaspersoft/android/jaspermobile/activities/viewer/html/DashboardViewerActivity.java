/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.webview.WebFlowFactory;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.webview.WebFlowStrategy;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.webview.settings.GeneralWebViewSettings;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.JSWebViewClient;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * Activity that performs dashboard viewing in HTML format.
 *
 * @author Ivan Gadzhega
 * @since 1.4
 */
@EActivity(R.layout.activity_dashboard_viewer)
@OptionsMenu(R.menu.dashboard_menu)
public class DashboardViewerActivity extends RoboSpiceActivity {

    @OptionsMenuItem
    protected MenuItem favoriteAction;

    @Extra
    protected ResourceLookup resource;

    @Bean
    protected FavoritesHelper favoritesHelper;
    @Bean
    protected JSWebViewClient jsWebViewClient;

    @InstanceState
    protected Uri favoriteEntryUri;

    @ViewById
    protected WebView webView;
    @ViewById
    protected ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            favoriteEntryUri = favoritesHelper.queryFavoriteUri(resource);
        }
    }

    @AfterViews
    final void init() {
        CookieManagerFactory.syncCookies(this);

        GeneralWebViewSettings.configure(webView);
        webView.setWebChromeClient(new ChromeClient());
        webView.setWebViewClient(new WebClient(jsWebViewClient));

        WebFlowStrategy webFlow = WebFlowFactory.getInstance(this).createStrategy();
        webFlow.load(webView, resource.getUri());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        favoriteAction.setIcon(favoriteEntryUri == null ? R.drawable.ic_star_outline : R.drawable.ic_star);
        favoriteAction.setTitle(favoriteEntryUri == null ? R.string.r_cm_add_to_favorites : R.string.r_cm_remove_from_favorites);
        return result;
    }

    @OptionsItem
    final void favoriteAction() {
        favoriteEntryUri = favoritesHelper.
                handleFavoriteMenuAction(favoriteEntryUri, resource, favoriteAction);
    }

    @OptionsItem
    final void refreshAction() {
        WebFlowStrategy webFlow = WebFlowFactory.getInstance(this).createStrategy();
        webFlow.load(webView, resource.getUri());
    }

    @OptionsItem
    final void aboutAction() {
        SimpleDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setTitle(resource.getLabel())
                .setMessage(resource.getDescription())
                .setNegativeButtonText(android.R.string.ok)
                .show();
    }

    private static class WebClient extends WebViewClient {
        private final WebViewClient mDecoratedClient;

        WebClient(WebViewClient decoratedClient) {
            mDecoratedClient = decoratedClient;
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            return mDecoratedClient.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            String js = "console.log( \"ready!\" );";
            view.loadUrl("javascript:" + js);
        }
    }

    private class ChromeClient extends WebChromeClient {
        public void onProgressChanged(WebView view, int progress) {
            int maxProgress = progressBar.getMax();
            progressBar.setProgress((maxProgress / 100) * progress);
            if (progress == maxProgress) {
                progressBar.setVisibility(View.GONE);
            }
        }
    }
}
