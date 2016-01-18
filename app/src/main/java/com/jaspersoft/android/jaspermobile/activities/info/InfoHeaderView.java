package com.jaspersoft.android.jaspermobile.activities.info;

import android.support.design.widget.CollapsingToolbarLayout;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.ResourceView;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class InfoHeaderView implements ResourceView {

    private TopCropImageView imageView;
    private CollapsingToolbarLayout toolbarLayout;

    public InfoHeaderView(TopCropImageView imageView, CollapsingToolbarLayout toolbarLayout) {
        this.imageView = imageView;
        this.toolbarLayout = toolbarLayout;
    }

    @Override
    public void setTitle(CharSequence title) {
        toolbarLayout.setTitle(title);
    }

    @Override
    public void setSubTitle(CharSequence subTitle) {

    }

    @Override
    public TopCropImageView getImageView() {
        return imageView;
    }
}
