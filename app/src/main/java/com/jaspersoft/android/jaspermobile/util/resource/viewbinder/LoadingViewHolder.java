package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;

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
    public TopCropImageView getImageView() {
        return null;
    }

    @Override
    public void setSecondaryAction(int actionImage) {

    }
}
