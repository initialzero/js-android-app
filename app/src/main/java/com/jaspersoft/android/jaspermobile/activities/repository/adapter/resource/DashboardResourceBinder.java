package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import android.content.Context;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class DashboardResourceBinder extends BaseResourceBinder {
    public DashboardResourceBinder(Context context) {
        super(context);
    }

    @Override
    public void setIcon(ImageView imageView, String uri) {
        ((TopCropImageView) imageView).setScaleType(TopCropImageView.ScaleType.FIT_XY);
        imageView.setBackgroundResource(R.drawable.js_blue_gradient);
        imageView.setImageResource(R.drawable.sample_dashboard_blue);
    }
}
