package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.accounts.Account;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.ImageView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import roboguice.RoboGuice;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class ReportResourceBinder extends ResourceBinder {
    private final int mAnimationSpeed;
    private final boolean isAmberOrHigher;

    private DisplayImageOptions displayImageOptions;

    @Inject
    protected JsRestClient jsRestClient;

    public ReportResourceBinder(Context context) {
        super(context);
        RoboGuice.getInjector(context).injectMembersWithoutViews(this);

        Account account = JasperAccountManager.get(context).getActiveAccount();
        AccountServerData serverData = AccountServerData.get(context, account);
        ServerRelease serverRelease = ServerRelease.parseVersion(serverData.getVersionName());
        isAmberOrHigher = serverRelease.code() >= ServerRelease.AMBER.code();

        mAnimationSpeed = context.getResources().getInteger(
                android.R.integer.config_mediumAnimTime);
    }

    @Override
    public void setIcon(ImageView imageView, String uri) {
        imageView.setBackgroundResource(R.drawable.bg_gradient_grey);

        if (isAmberOrHigher) {
            loadFromNetwork(imageView, uri);
        } else {
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
            imageView.setImageResource(R.drawable.placeholder_report);
        }
    }

    private void loadFromNetwork(ImageView imageView, String uri) {
        String path = jsRestClient.generateThumbNailUri(uri);
        ImageLoader.getInstance().displayImage(
                path, imageView, getDisplayImageOptions(),
                new ImageLoadingListener()
        );
    }

    private DisplayImageOptions getDisplayImageOptions() {
        if (displayImageOptions == null) {
            displayImageOptions = new DisplayImageOptions.Builder()
                    .showImageOnLoading(R.drawable.placeholder_report)
                    .showImageForEmptyUri(R.drawable.placeholder_report)
                    .showImageOnFail(R.drawable.placeholder_report)
                    .considerExifParams(true)
                    .cacheInMemory(true)
                    .cacheOnDisk(true)
                    .bitmapConfig(Bitmap.Config.RGB_565)
                    .displayer(new FadeInBitmapDisplayer(mAnimationSpeed))
                    .build();
        }
        return displayImageOptions;
    }

    private static class ImageLoadingListener extends SimpleImageLoadingListener {
        @Override
        public void onLoadingStarted(String imageUri, View view) {
            ((TopCropImageView) view).setScaleType(TopCropImageView.ScaleType.FIT_CENTER);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            ((TopCropImageView) view).setScaleType(TopCropImageView.ScaleType.MATRIX);
            ((TopCropImageView) view).setScaleType(TopCropImageView.ScaleType.TOP_CROP);
        }
    }

}
