package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class ReportResourceBinder extends BaseResourceBinder {
    public ReportResourceBinder(Context context) {
        super(context);
    }

    @Override
    public int getResourceIcon() {
        return R.drawable.sample_report_grey;
    }

    @Override
    public int getResourceBackground() {
        return R.drawable.js_grey_gradient;
    }
}
