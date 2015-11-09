package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.widget.ImageView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
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
    private GetFileResourceRequest mFileResourceRequest;

    public FileResourceBinder(Context context) {
        super(context);
        mSpiceManager = ((RoboSpiceActivity) getContext()).getSpiceManager();
        RoboGuice.getInjector(context).injectMembersWithoutViews(this);
    }

    @Override
    public void setIcon(TopCropImageView imageView, String uri) {
        imageView.setScaleType(TopCropImageView.ScaleType.CENTER);
        imageView.setBackgroundResource(R.drawable.bg_gradient_grey);
        imageView.setImageResource(R.drawable.ic_file);
        loadFileType(imageView, uri);
    }

    @Override
    public void unbindView() {
        if (mFileResourceRequest != null) {
            mFileResourceRequest.cancel();
        }
    }

    private void loadFileType(final ImageView imageView, String uri) {
        mFileResourceRequest = new GetFileResourceRequest(jsRestClient, uri);
        long cacheExpiryDuration = DefaultPrefHelper_.getInstance_(getContext()).getRepoCacheExpirationValue();
        mSpiceManager.execute(mFileResourceRequest, mFileResourceRequest.createCacheKey(), cacheExpiryDuration, new SimpleRequestListener<FileLookup>() {
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
                    case csv:
                        resId = R.drawable.ic_file_csv;
                        break;
                    case docx:
                        resId = R.drawable.ic_file_doc;
                        break;
                    case html:
                        resId = R.drawable.ic_file_html;
                        break;
                    case img:
                        resId = R.drawable.ic_file_img;
                        break;
                    case json:
                        resId = R.drawable.ic_file_json;
                        break;
                    case ods:
                        resId = R.drawable.ic_file_ods;
                        break;
                    case odt:
                        resId = R.drawable.ic_file_odt;
                        break;
                    case pdf:
                        resId = R.drawable.ic_file_pdf;
                        break;
                    case pptx:
                        resId = R.drawable.ic_file_pptx;
                        break;
                    case xls:
                    case xlsx:
                        resId = R.drawable.ic_file_xls;
                        break;
                    default:
                        resId = R.drawable.ic_file;
                        break;
                }
                imageView.setImageResource(resId);
            }
        });
    }
}
