package com.jaspersoft.android.jaspermobile.internal.di.modules;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.data.cache.report.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.ServerCache;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.sdk.network.AuthorizedClient;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.network.SpringCredentials;
import com.jaspersoft.android.sdk.service.report.ReportService;
import com.jaspersoft.android.sdk.service.rx.report.RxFiltersService;
import com.jaspersoft.android.sdk.service.rx.report.RxReportService;

import java.net.CookieManager;
import java.util.concurrent.TimeUnit;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class ProfileModule {
    private final Profile mProfile;

    public ProfileModule(Profile profile) {
        mProfile = profile;
    }


    @Provides
    @PerProfile
    Server provideServer(Context context, ServerCache serverCache) {
        JasperServer server = serverCache.get(mProfile);

        DefaultPrefHelper prefHelper = DefaultPrefHelper_.getInstance_(context);
        int connectTimeout = prefHelper.getConnectTimeoutValue();
        int readTimeout = prefHelper.getReadTimeoutValue();

        return Server.builder()
                .withBaseUrl(server.getBaseUrl() + "/")
                .withConnectionTimeOut(connectTimeout, TimeUnit.MILLISECONDS)
                .withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    @Provides
    @PerProfile
    ReportService provideReportService(AuthorizedClient authorizedClient) {
        return ReportService.newService(authorizedClient);
    }

    @Provides
    @PerProfile
    RxReportService provideRxReportService(AuthorizedClient authorizedClient) {
        return RxReportService.newService(authorizedClient);
    }

    @Provides
    @PerProfile
    RxFiltersService provideRxFiltersService(AuthorizedClient authorizedClient) {
        return RxFiltersService.newService(authorizedClient);
    }

    @Provides
    @PerProfile
    AuthorizedClient provideAuthorizedClient(Server server, CredentialsCache credentialsCache) {
        AppCredentials appCredentials = credentialsCache.get(mProfile);
        Credentials credentials = SpringCredentials.builder()
                .withUsername(appCredentials.getUsername())
                .withPassword(appCredentials.getPassword())
                .withOrganization(appCredentials.getOrganization())
                .build();

        return server.newClient(credentials)
                .withCookieHandler(CookieManager.getDefault())
                .create();
    }

}
