package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.client.oxm.report.FolderDataResponse;
import com.jaspersoft.android.sdk.client.oxm.resource.FileLookup;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.data.report.FileResource;
import com.jaspersoft.android.sdk.service.data.repository.Resource;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public class ResourceMapper {
    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Inject
    public ResourceMapper() {
    }

    @NonNull
    public List<FolderDataResponse> toLegacyFolders(@NonNull List<Resource> resources) {
        List<FolderDataResponse> list = new ArrayList<>(resources.size());
        for (Resource resource : resources) {
            if (resource != null) {
                FolderDataResponse folder = new FolderDataResponse();
                toLegacyResource(resource, folder);
                list.add(folder);
            }
        }
        return list;
    }

    @NonNull
    public List<ResourceLookup> toLegacyResources(@NonNull List<Resource> resources) {
        List<ResourceLookup> list = new ArrayList<>(resources.size());
        for (Resource resource : resources) {
            if (resource != null) {
                ResourceLookup lookup = new ResourceLookup();
                toLegacyResource(resource, lookup);
                list.add(lookup);
            }
        }
        return list;
    }

    public ResourceLookup toConcreteLegacyResource(@NonNull Resource resource, @NonNull String type)
            throws Exception {
        ResourceLookup lookup = new ResourceLookup();
        if ("file".equals(type)) {
            FileResource fileResource = (FileResource) resource;
            FileResource.Type fileType = fileResource.getType();
            String legacyFileType = fileType.name();

            FileLookup fileLookup = new FileLookup();
            fileLookup.setFileType(legacyFileType);
            lookup = fileLookup;
        }
        toLegacyResource(resource, lookup);
        return lookup;
    }

    public void toLegacyResource(@NonNull Resource resource, @NonNull ResourceLookup lookup) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.getDefault());
        String creationDate = simpleDateFormat.format(resource.getCreationDate());
        String updateDate = simpleDateFormat.format(resource.getUpdateDate());


        String rawValue = resource.getResourceType().getRawValue();
        ResourceLookup.ResourceType resourceType = ResourceLookup.ResourceType.valueOf(rawValue);
        lookup.setResourceType(resourceType);

        lookup.setLabel(resource.getLabel());
        lookup.setDescription(resource.getDescription());
        lookup.setUri(resource.getUri());
        lookup.setVersion(resource.getVersion());
        lookup.setCreationDate(creationDate);
        lookup.setUpdateDate(updateDate);
        lookup.setPermissionMask(resource.getPermissionMask().getMask());
    }
}
