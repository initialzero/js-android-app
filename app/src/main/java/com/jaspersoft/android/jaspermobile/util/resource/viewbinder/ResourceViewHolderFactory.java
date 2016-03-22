/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class ResourceViewHolderFactory {

    private Context mContext;

    public ResourceViewHolderFactory(Context mContext) {
        this.mContext = mContext;
    }

    public BaseResourceViewHolder create(ViewGroup parentView, int viewType) {
        BaseResourceViewHolder resourceViewHolder;
        switch (viewType) {
            case JasperResourceAdapter.LOADING_TYPE:
                View loadingView = LayoutInflater.from(mContext).
                        inflate(R.layout.item_resource_list_loading, parentView, false);
                resourceViewHolder = new LoadingViewHolder(loadingView);
                break;
            case JasperResourceAdapter.LIST_TYPE:
                View listResourceView = LayoutInflater.from(mContext).
                        inflate(R.layout.item_resource_list, parentView, false);
                resourceViewHolder = new ListResourceViewHolder(listResourceView);
                break;
            case JasperResourceAdapter.GRID_TYPE:
                View gridResourceView = LayoutInflater.from(mContext).
                        inflate(R.layout.item_resource_grid, parentView, false);
                resourceViewHolder = new GridResourceViewHolder(gridResourceView);
                break;
            default:
                View defaultResourceView = LayoutInflater.from(mContext).
                        inflate(R.layout.item_resource_list, parentView, false);
                resourceViewHolder = new ListResourceViewHolder(defaultResourceView);
        }
        return resourceViewHolder;
    }
}
