package com.jaspersoft.android.jaspermobile.data;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.data.cache.profile.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.JasperServerCache;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.sdk.network.AuthorizedClient;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.network.SpringCredentials;
import com.jaspersoft.android.sdk.service.rx.filter.RxFiltersService;
import com.jaspersoft.android.sdk.service.rx.report.RxReportService;
import com.jaspersoft.android.sdk.service.rx.repository.RxRepositoryService;

import java.net.CookieManager;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class JasperClient implements JasperRestClient {
    private final Context mContext;
    private final Profile mProfile;
    private final CredentialsCache mCredentialsCache;
    private final JasperServerCache mServerCache;

    @Inject
    public JasperClient(
            @ApplicationContext Context context,
            Profile profile,
            CredentialsCache credentialsCache,
            JasperServerCache serverCache
    ) {
        mContext = context;
        mProfile = profile;
        mCredentialsCache = credentialsCache;
        mServerCache = serverCache;
    }

    @Override
    public Observable<RxReportService> reportService() {
        return createAuthorizedClient().map(new Func1<AuthorizedClient, RxReportService>() {
            @Override
            public RxReportService call(AuthorizedClient authorizedClient) {
                return RxReportService.newService(authorizedClient);
            }
        });
    }

    @Override
    public Observable<RxRepositoryService> repositoryService() {
        return createAuthorizedClient().map(new Func1<AuthorizedClient, RxRepositoryService>() {
            @Override
            public RxRepositoryService call(AuthorizedClient authorizedClient) {
                return RxRepositoryService.newService(authorizedClient);
            }
        });
    }

    @Override
    public Observable<RxFiltersService> filtersService() {
        return createAuthorizedClient().map(new Func1<AuthorizedClient, RxFiltersService>() {
            @Override
            public RxFiltersService call(AuthorizedClient authorizedClient) {
                return RxFiltersService.newService(authorizedClient);
            }
        });
    }

    private Observable<AuthorizedClient> createAuthorizedClient() {
        return Observable.defer(new Func0<Observable<AuthorizedClient>>() {
            @Override
            public Observable<AuthorizedClient> call() {
                AppCredentials appCredentials = mCredentialsCache.get(mProfile);
                Credentials credentials = SpringCredentials.builder()
                        .withUsername(appCredentials.getUsername())
                        .withPassword(appCredentials.getPassword())
                        .withOrganization(appCredentials.getOrganization())
                        .build();
                AuthorizedClient authorizedClient = provideServer().newClient(credentials)
                        .withCookieHandler(CookieManager.getDefault())
                        .create();
                return Observable.just(authorizedClient);
            }
        });
    }

    private Server provideServer() {
        DefaultPrefHelper prefHelper = DefaultPrefHelper_.getInstance_(mContext);
        int connectTimeout = prefHelper.getConnectTimeoutValue();
        int readTimeout = prefHelper.getReadTimeoutValue();

        JasperServer server = providesJasperServer();

        return Server.builder()
                .withBaseUrl(server.getBaseUrl() + "/")
                .withConnectionTimeOut(connectTimeout, TimeUnit.MILLISECONDS)
                .withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .build();
    }

    private JasperServer providesJasperServer() {
        return mServerCache.get(mProfile);
    }
}
