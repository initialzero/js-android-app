package com.jaspersoft.android.jaspermobile.internal.di.modules.activity;

import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.util.print.DashboardPicturePrintJob;
import com.jaspersoft.android.jaspermobile.util.print.DashboardViewPrintJob;
import com.jaspersoft.android.jaspermobile.util.print.ResourcePrintJob;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class DashboardModule {
    private final WebView mWebView;
    private final String mType;

    public DashboardModule(WebView webView,
                           String type) {
        mWebView = webView;
        mType = type;
    }

    @Provides
    @PerActivity
    ResourcePrintJob providePrintJob(JasperServer server) {
        String versionName = server.getVersion();
        ServerVersion version = ServerVersion.valueOf(versionName);
        if (version.lessThan(ServerVersion.v6) || "legacyDashboard".equals(mType)) {
            return new DashboardPicturePrintJob(mWebView);
        }
        return new DashboardViewPrintJob(mWebView);
    }
}
