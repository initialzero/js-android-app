package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Tom Koptel
 * @since 2.0
 */
class DashboardResourceAsset implements ResourceAsset {
    @Override
    public int getResourceIcon() {
        return R.drawable.sample_dashboard_blue;
    }

    @Override
    public int getResourceBackground() {
        return R.drawable.js_blue_gradient;
    }
}
