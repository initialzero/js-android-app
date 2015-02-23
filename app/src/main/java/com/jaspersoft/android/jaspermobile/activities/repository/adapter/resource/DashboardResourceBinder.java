package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class DashboardResourceBinder extends BaseResourceBinder {
    public DashboardResourceBinder(Context context) {
        super(context);
    }

    @Override
    public int getResourceIcon() {
        return R.drawable.sample_dashboard_blue;
    }

    @Override
    public int getResourceBackground() {
        return R.drawable.js_blue_gradient;
    }
}
