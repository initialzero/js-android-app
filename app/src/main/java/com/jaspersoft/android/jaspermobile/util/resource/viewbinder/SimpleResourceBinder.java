package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceView;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
abstract class SimpleResourceBinder implements ResourceBinder {
    private static final String LOG_TAG = SimpleResourceBinder.class.getSimpleName();
    private final Context mContext;

    public SimpleResourceBinder(Context context) {
        mContext = context;
        Timber.tag(SimpleResourceBinder.LOG_TAG);
    }

    @Override
    public void bindView(ResourceView resourceView, ResourceAdapter.KpiResourceLookup lookup) {
        ResourceLookup item = lookup.getResource();

        ImageView kpiImage = resourceView.getKpiImage();
        if (kpiImage != null) {
            kpiImage.setVisibility(View.GONE);
        }

        setIcon(resourceView.getImageView(), item.getUri());
        resourceView.getTitleView().setText(item.getLabel());

        if (resourceView.getSubTitleView() != null) {
            resourceView.getSubTitleView().setText(item.getDescription());
        }
    }

    public Context getContext() {
        return mContext;
    }

    public abstract void setIcon(ImageView imageView, String uri);
}
