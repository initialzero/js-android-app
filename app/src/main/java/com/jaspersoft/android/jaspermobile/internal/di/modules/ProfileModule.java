package com.jaspersoft.android.jaspermobile.internal.di.modules;

import com.jaspersoft.android.jaspermobile.data.JasperClient;
import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.cache.profile.CredentialsCache;
import com.jaspersoft.android.jaspermobile.data.cache.profile.JasperServerCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ControlsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryControlsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryReportCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryReportPageCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryReportPropertyCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPageCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPropertyCache;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryControlsRepository;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportOptionsRepository;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportPageRepository;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportRepository;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryResourceRepository;
import com.jaspersoft.android.jaspermobile.domain.AppCredentials;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportOptionsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.network.AuthorizedClient;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.network.SpringCredentials;

import java.net.CookieManager;

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
    Profile providesProfile() {
        return mProfile;
    }

    @Provides
    JasperRestClient provideJasperRestClient(JasperClient client) {
        return client;
    }

    @Provides
    @PerProfile
    JasperServer providesJasperServer(JasperServerCache jasperServerCache) {
        return jasperServerCache.get(mProfile);
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

    @Provides
    @PerProfile
    ReportOptionsRepository providesReportOptionsRepository(InMemoryReportOptionsRepository repository) {
        return repository;
    }

    @Provides
    @PerProfile
    ResourceRepository providesResourceRepository(InMemoryResourceRepository repository) {
        return repository;
    }

    @Provides
    @PerProfile
    ReportRepository providesReportRepository(InMemoryReportRepository reportRepository) {
        return reportRepository;
    }

    @Provides
    @PerProfile
    ControlsRepository providesControlsRepository(InMemoryControlsRepository controlsRepository) {
        return controlsRepository;
    }

    @Provides
    @PerProfile
    ReportPageRepository providesReportPageRepository(InMemoryReportPageRepository memoryReportPageRepository) {
        return memoryReportPageRepository;
    }

    @Provides
    @PerProfile
    ReportPropertyRepository providesReportPropertyRepository(InMemoryReportPropertyRepository reportPropertyRepository) {
        return reportPropertyRepository;
    }

    @Provides
    @PerProfile
    ControlsCache providesControlsCache(InMemoryControlsCache controlsCache) {
        return controlsCache;
    }

    @Provides
    @PerProfile
    ReportCache providesReportCache(InMemoryReportCache reportCache) {
        return reportCache;
    }

    @Provides
    @PerProfile
    ReportPropertyCache providesReportPropertyCache(InMemoryReportPropertyCache cache) {
        return cache;
    }

    @Provides
    @PerProfile
    ReportParamsCache providesReportParamsCache(InMemoryReportParamsCache reportParamsCache) {
        return reportParamsCache;
    }

    @Provides
    @PerProfile
    ReportPageCache providesReportPageCache(InMemoryReportPageCache reportPageCache) {
        return reportPageCache;
    }
}
