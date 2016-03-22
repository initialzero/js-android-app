package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;

import com.nostra13.universalimageloader.core.process.BitmapProcessor;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class CustomBitmapProcessor implements BitmapProcessor {
    private final Resources mResources;
    private final int mResId;
    private final Context mContext;

    CustomBitmapProcessor(Context context, @DrawableRes int resId) {
        mContext = context;
        mResources = context.getResources();
        mResId = resId;
    }

    @Override
    public Bitmap process(Bitmap bitmap) {
        if (atLeast12x12(bitmap)) {
            return removeArtifacts(bitmap);
        }
        return staticThumbnail();
    }

    private Bitmap removeArtifacts(Bitmap origin) {
        int originalWidth = origin.getWidth();
        int originalHeight = origin.getHeight();

        int ratioHeight = calculateRatioHeight(originalWidth, originalHeight);
        int newHeight = ratioHeight - 6;
        int newWidth = originalWidth - 6;

        if (newWidth > 0 && newHeight > 0) {
            return Bitmap.createBitmap(origin, 3, 3, newWidth, newHeight);
        }
        return origin;
    }

    private Bitmap staticThumbnail() {
        Drawable drawable = ContextCompat.getDrawable(mContext, mResId);
        return convertToBitmap(drawable);
    }

    public Bitmap convertToBitmap(Drawable drawable) {
        int widthPixels = drawable.getIntrinsicWidth();
        int heightPixels = drawable.getIntrinsicHeight();

        Bitmap mutableBitmap = Bitmap.createBitmap(widthPixels, heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(mutableBitmap);
        drawable.setBounds(0, 0, widthPixels, heightPixels);
        drawable.draw(canvas);

        return mutableBitmap;
    }

    private int calculateRatioHeight(int originalWidth, int originalHeight) {
        int scaledWidth = (int) (originalWidth * 0.66);
        return scaledWidth < originalHeight ? scaledWidth : originalHeight;
    }

    private boolean atLeast12x12(Bitmap bitmap) {
        return bitmap.getWidth() >= 12 && bitmap.getHeight() >= 12;
    }
}
