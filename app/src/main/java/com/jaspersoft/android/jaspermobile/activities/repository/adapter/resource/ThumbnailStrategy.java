package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import android.widget.ImageView;

/**
 * @author Tom Koptel
 * @since 2.0
 */
interface ThumbnailStrategy {
    void setIcon(ImageView imageView, String uri);
}
