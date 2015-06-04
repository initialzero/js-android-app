package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;

/**
 * @author Tom Koptel
 * @since 2.0
 */
class FolderResourceBinder extends ResourceBinder {

    public FolderResourceBinder(Context context) {
        super(context);
    }

    @Override
    public void setIcon(ImageView imageView, String uri) {
        ((TopCropImageView) imageView).setScaleType(TopCropImageView.ScaleType.FIT_XY);
        imageView.setBackgroundResource(R.drawable.bg_gradient_blue);
        imageView.setImageResource(R.drawable.placeholder_folder);
    }
}