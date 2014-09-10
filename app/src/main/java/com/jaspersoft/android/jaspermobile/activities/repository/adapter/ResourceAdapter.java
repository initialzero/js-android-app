/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.repository.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import static com.google.common.base.Preconditions.checkNotNull;

public class ResourceAdapter extends ArrayAdapter<ResourceLookup> {
    private ResourceViewHelper viewHelper = new ResourceViewHelper();

    private final ViewType mViewType;

    public static Builder builder(Context context) {
        checkNotNull(context);
        return new Builder(context);
    }

    private ResourceAdapter(Context context, ViewType viewType) {
        super(context, 0);
        mViewType = checkNotNull(viewType, "ViewType can`t be null");
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewImpl(position, (IResourceView) convertView);
    }

    private View getViewImpl(int position, IResourceView convertView) {
        IResourceView itemView;
        if (mViewType == ViewType.LIST) {
            itemView = convertView;
        } else {
            itemView = convertView;
        }

        if (itemView == null) {
            if (mViewType == ViewType.LIST) {
                itemView = ListItemView_.build(getContext());
            } else {
                itemView = GridItemView_.build(getContext());
            }
        }

        viewHelper.populateView(itemView, getItem(position));
        return (View) itemView;
    }

    public static class Builder {
        private final Context context;

        private ViewType viewType;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setViewType(ViewType viewType) {
            this.viewType = viewType;
            return this;
        }

        public ResourceAdapter create() {
            return new ResourceAdapter(context, viewType);
        }
    }
}
