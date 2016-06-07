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

package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */

/**
 * Simple View Holder for grid representation of resource item. View consist of icon and title.
 */
public class GridResourceViewHolder extends BaseResourceViewHolder {
    protected ImageView ivIcon;
    protected TextView tvName;
    protected ImageButton btnSecondaryAction;

    public GridResourceViewHolder(View itemView) {
        super(itemView);

        this.ivIcon = (ImageView) itemView.findViewById(android.R.id.icon);
        this.tvName = (TextView) itemView.findViewById(android.R.id.text1);
        this.btnSecondaryAction = (ImageButton) itemView.findViewById(R.id.secondaryAction);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemInteractionListener != null) {
                    mItemInteractionListener.onViewSingleClick(getAdapterPosition());
                }
            }
        });

        btnSecondaryAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemInteractionListener != null) {
                    mItemInteractionListener.onSecondaryActionClick(getAdapterPosition());
                }
            }
        });
    }

    @Override
    public void setTitle(CharSequence title) {
        tvName.setText(title);
    }

    @Override
    public void setSubTitle(CharSequence subTitle) {

    }

    @Override
    public ImageView getImageView() {
        return ivIcon;
    }

    @Override
    public boolean isImageThumbnail() {
        return true;
    }

    @Override
    public void setSecondaryAction(int actionImage) {
        btnSecondaryAction.setVisibility(actionImage == 0 ? View.GONE : View.VISIBLE);
        btnSecondaryAction.setImageResource(actionImage);
    }
}
