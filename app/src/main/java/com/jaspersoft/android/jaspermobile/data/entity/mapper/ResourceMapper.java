package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import com.jaspersoft.android.jaspermobile.domain.AppResource;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class ResourceMapper {
    @Inject
    public ResourceMapper() {
    }

    public AppResource mapReportResource(ReportResource resource) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
