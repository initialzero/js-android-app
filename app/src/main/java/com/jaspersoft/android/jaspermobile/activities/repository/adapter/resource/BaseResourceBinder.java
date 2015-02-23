package com.jaspersoft.android.jaspermobile.activities.repository.adapter.resource;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceView;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 1.9
 */
abstract class BaseResourceBinder implements ResourceBinder {

    private final Context mContext;

    public BaseResourceBinder(Context context) {
        mContext = context;
        Timber.tag(ResourceBinder.LOG_TAG);
    }

    @Override
    public void bindView(ResourceView resourceView, ResourceLookup item) {
        String type = item.getResourceType().toString();

        ThumbnailStrategy thumbnailStrategy = ThumbnailStrategyFactory.create(mContext, type);
        thumbnailStrategy.setIcon(resourceView.getImageView(), item.getUri());

        resourceView.setTitle(item.getLabel());
        resourceView.setSubTitle(item.getDescription());
    }

    public Context getContext() {
        return mContext;
    }

    public abstract int getResourceIcon();
    public abstract int getResourceBackground();
}
