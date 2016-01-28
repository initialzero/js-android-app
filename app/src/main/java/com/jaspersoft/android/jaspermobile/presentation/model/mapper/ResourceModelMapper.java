package com.jaspersoft.android.jaspermobile.presentation.model.mapper;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.AppResource;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.presentation.model.ReportResourceModel;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class ResourceModelMapper {

    @Inject
    public ResourceModelMapper() {
    }

    @NonNull
    public ReportResourceModel mapReportModel(AppResource resource) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
