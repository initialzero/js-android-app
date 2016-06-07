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

package com.jaspersoft.android.sdk.client.oxm;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a resource property entity for convenient XML serialization process.
 *
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */

public class ResourceProperty {

    @Expose
    private String name;

    @Expose
    private String value;

    @Expose
    private List<ResourceProperty> properties = new ArrayList<ResourceProperty>();


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public List<ResourceProperty> getProperties() {
        return properties;
    }

    public void setProperties(List<ResourceProperty> properties) {
        this.properties = properties;
    }

    public String toString() {
        return value;
    }
}
