package com.jaspersoft.android.jaspermobile.internal.di.modules;

import com.jaspersoft.android.jaspermobile.data.cache.report.ControlsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryControlsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryReportCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryReportPageCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.InMemoryReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportPageCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryControlsRepository;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportPageRepository;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.data.repository.report.InMemoryReportRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPropertyRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerReport;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class ReportModule {
    private final String mUri;

    public ReportModule(String uri) {
        mUri = uri;
    }

    @Provides
    @PerReport
    ReportRepository providesReportRepository(InMemoryReportRepository reportRepository) {
        return reportRepository;
    }

    @Provides
    @PerReport
    ControlsRepository providesControlsRepository(InMemoryControlsRepository controlsRepository) {
        return controlsRepository;
    }

    @Provides
    @PerReport
    ReportPageRepository providesReportPageRepository(InMemoryReportPageRepository memoryReportPageRepository) {
        return memoryReportPageRepository;
    }

    @Provides
    @PerReport
    ReportPropertyRepository providesReportPropertyRepository(InMemoryReportPropertyRepository reportPropertyRepository) {
        return reportPropertyRepository;
    }

    @Provides
    @PerReport
    ControlsCache providesControlsCache(InMemoryControlsCache controlsCache) {
        return controlsCache;
    }

    @Provides
    @PerReport
    ReportCache providesReportCache(InMemoryReportCache reportCache) {
        return reportCache;
    }

    @Provides
    @PerReport
    ReportParamsCache providesReportParamsCache(InMemoryReportParamsCache reportParamsCache) {
        return reportParamsCache;
    }

    @Provides
    @PerReport
    ReportPageCache providesReportPageCache(InMemoryReportPageCache reportPageCache) {
        return reportPageCache;
    }

    @Provides
    @Named("report_uri")
    @PerReport
    String provideUri() {
        return mUri;
    }
}
