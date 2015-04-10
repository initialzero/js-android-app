package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.text.TextUtils;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceView;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
abstract class ResourceBinder {
    private static final String LOG_TAG = ResourceBinder.class.getSimpleName();
    private final Context mContext;
    private ResourceAdapter.KpiResourceLookup mLookup;

    public ResourceBinder(Context context) {
        mContext = context;
        Timber.tag(ResourceBinder.LOG_TAG);
    }

    public void bindView(ResourceView resourceView, ResourceAdapter.KpiResourceLookup lookup) {
        mLookup = lookup;
        ResourceLookup item = lookup.getResource();
        setIcon(resourceView.getImageView(), item.getUri());
        resourceView.setTitle(item.getLabel());
        resourceView.setSubTitle(item.getDescription());
    }

    protected boolean isKpi() {
        if (mLookup == null) {
            return false;
        } else {
            return !TextUtils.isEmpty(mLookup.getKpiUri());
        }
    }

    public Context getContext() {
        return mContext;
    }

    public abstract void setIcon(ImageView imageView, String uri);
}
