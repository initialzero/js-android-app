/*
* Copyright © 2015 TIBCO Software, Inc. All rights reserved.
* http://community.jaspersoft.com/project/jaspermobile-android
*
* Unless you have purchased a commercial license agreement from Jaspersoft,
* the following license terms apply:
*
* This program is part of Jaspersoft Mobile for Android.
*
* Jaspersoft Mobile is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Jaspersoft Mobile is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with Jaspersoft Mobile for Android. If not, see
* <http://www.gnu.org/licenses/lgpl>.
*/

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
