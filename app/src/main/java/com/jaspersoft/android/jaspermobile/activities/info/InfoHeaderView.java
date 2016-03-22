package com.jaspersoft.android.jaspermobile.activities.info;

import android.support.design.widget.CollapsingToolbarLayout;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.ResourceView;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class InfoHeaderView implements ResourceView {

    private ImageView imageView;
    private CollapsingToolbarLayout toolbarLayout;

    public InfoHeaderView(ImageView imageView, CollapsingToolbarLayout toolbarLayout) {
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
    public ImageView getImageView() {
        return imageView;
    }

    @Override
    public boolean isImageThumbnail() {
        return true;
    }

    @Override
    public void setSecondaryAction(int actionImage) {

    }
}
