package com.jaspersoft.android.jaspermobile.test.utils;

import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class DummyResourceUtils {
    public static final String RESOURCE_DEFAULT_URI = "/Reports/3_Store_Segment_Performance_Report";
    public static final String RESOURCE_DEFAULT_LABEL = "03. Store Segment Performance Report";

    public static final String RESOURCE_IC_URI = "/Reports/1._Geographic_Results_by_Segment_Report";
    public static final String RESOURCE_IC_LABEL = "01. Geographic Results by Segment Report";

    public static ResourceLookup createDefaultLookup() {
        ResourceLookup resource = new ResourceLookup();
        resource.setLabel(RESOURCE_DEFAULT_LABEL);
        resource.setUri(RESOURCE_DEFAULT_URI);
        resource.setResourceType(ResourceLookup.ResourceType.reportUnit.toString());
        return resource;
    }

    public static ResourceLookup createLookupWithIC() {
        ResourceLookup resource = new ResourceLookup();
        resource.setLabel(RESOURCE_IC_URI);
        resource.setUri(RESOURCE_IC_LABEL);
        resource.setResourceType(ResourceLookup.ResourceType.reportUnit.toString());
        return resource;
    }

}
