package com.jaspersoft.android.jaspermobile.presentation.model.mapper;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.presentation.model.ReportResourceModel;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;

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
    public ReportResourceModel mapReportModel(ReportResource resource) {
        return new ReportResourceModel.Builder()
                .setLabel(resource.getLabel())
                .setDescription(resource.getDescription())
                .setUri(resource.getUri())
                .setCreationDate(resource.getCreationDate())
                .build();
    }
}
