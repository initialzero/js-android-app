package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.widget.ImageView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetFileResourceRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.FileLookup;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.exception.SpiceException;

import roboguice.RoboGuice;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class FileResourceBinder extends ResourceBinder {

    private final SpiceManager mSpiceManager;
    @Inject
    protected JsRestClient jsRestClient;

    public FileResourceBinder(Context context) {
        super(context);
        mSpiceManager = ((RoboSpiceActivity) getContext()).getSpiceManager();
        RoboGuice.getInjector(context).injectMembersWithoutViews(this);
    }

    @Override
    public void setIcon(ImageView imageView, String uri) {
        imageView.setBackgroundResource(R.drawable.bg_gradient_grey);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setImageResource(R.drawable.ic_file);
        loadFileType(imageView, uri);
    }

    private void loadFileType(final ImageView imageView, String uri) {
        GetFileResourceRequest fileResourceRequest = new GetFileResourceRequest(jsRestClient, uri);
        long cacheExpiryDuration = DefaultPrefHelper_.getInstance_(getContext()).getRepoCacheExpirationValue();
        mSpiceManager.execute(fileResourceRequest, fileResourceRequest.createCacheKey(), cacheExpiryDuration, new SimpleRequestListener<FileLookup>() {
            @Override
            protected Context getContext() {
                return FileResourceBinder.this.getContext();
            }

            @Override
            public void onRequestFailure(SpiceException spiceException) {

            }

            @Override
            public void onRequestSuccess(FileLookup fileLookup) {
                int resId;
                switch (fileLookup.getFileType()) {
                    case pdf:
                        resId = R.drawable.ic_saved_pdf;
                        break;
                    case xls:
                        resId = R.drawable.ic_saved_xls;
                        break;
                    case html:
                        resId = R.drawable.ic_saved_html;
                        break;
                    case img:
                        resId = R.drawable.ic_img;
                        break;
                    default :
                        resId = R.drawable.ic_undefined;
                        break;
                }
                imageView.setImageResource(resId);
            }
        });
    }
}
