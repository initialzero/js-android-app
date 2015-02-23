package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;

/**
 * @author Tom Koptel
 * @since 2.0
 */
class BaseThumbnailStrategy implements ThumbnailStrategy {
    private final ResourceAsset mResourceAsset;

    public BaseThumbnailStrategy(ResourceAsset resourceAsset) {
        mResourceAsset = resourceAsset;
    }

    @Override
    public void setIcon(ImageView imageView, String uri) {
        imageView.setBackgroundResource(mResourceAsset.getResourceBackground());
        ((TopCropImageView) imageView).setScaleType(TopCropImageView.ScaleType.FIT_XY);
        imageView.setImageResource(mResourceAsset.getResourceIcon());
    }
}
