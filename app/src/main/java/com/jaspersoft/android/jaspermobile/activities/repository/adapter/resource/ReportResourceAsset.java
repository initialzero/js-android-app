package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Tom Koptel
 * @since 2.0
 */
class ReportResourceAsset implements ResourceAsset {
    @Override
    public int getResourceIcon() {
        return R.drawable.sample_report_grey;
    }

    @Override
    public int getResourceBackground() {
        return R.drawable.js_grey_gradient;
    }
}
