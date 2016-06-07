/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.entity.JasperResource;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.repository.Resource;
import com.jaspersoft.android.sdk.service.data.repository.ResourceType;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
@PerProfile
public class ResourcesMapper {

    @Inject
    public ResourcesMapper() {
    }

    @NonNull
    public List<JasperResource> toJasperResources(@NonNull Collection<Resource> resources) {
        List<JasperResource> jasperResources = new ArrayList<>();
        for (Resource resource : resources) {
            JasperResource jasperResource = toJasperResource(resource);
            jasperResources.add(jasperResource);
        }
        return jasperResources;
    }

    @NonNull
    public JasperResource toJasperResource(@NonNull Resource resource) {
        URI resourceUri = URI.create(resource.getUri());
        JasperResource.Type type = parseResourceType(resource.getResourceType());
        return new JasperResource(resource.getLabel(), resourceUri, resource.getDescription(), type);
    }

    private JasperResource.Type parseResourceType(ResourceType resourceType) {
        switch (resourceType) {
            case reportUnit:
                return JasperResource.Type.report;
            case dashboard:
                return JasperResource.Type.dashboard;
            case legacyDashboard:
                return JasperResource.Type.legacyDashboard;
            case folder:
                return JasperResource.Type.folder;
            case file:
                return JasperResource.Type.file;
            default:
                return JasperResource.Type.undefined;
        }
    }
}
