package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.view.View;
import android.widget.ImageView;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class LoadingViewHolder extends BaseResourceViewHolder{

    public LoadingViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    public void setTitle(CharSequence title) {

    }

    @Override
    public void setSubTitle(CharSequence subTitle) {

    }

    @Override
    public ImageView getImageView() {
        return null;
    }

    @Override
    public boolean isImageThumbnail() {
        return false;
    }

    @Override
    public void setSecondaryAction(int actionImage) {

    }
}
