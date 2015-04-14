package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class DashboardResourceBinder extends SimpleResourceBinder {
    public DashboardResourceBinder(Context context) {
        super(context);
    }

    @Override
    public void setIcon(ImageView imageView, String uri) {
        ((TopCropImageView) imageView).setScaleType(TopCropImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundResource(R.drawable.bg_gradient_blue);
        imageView.setImageResource(R.drawable.placeholder_dashboard);
    }
}
