package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.graphics.Matrix;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
public class TopCropImageView extends ImageView {

    private ScaleType mSkaleType;

    public enum ScaleType {
        MATRIX      (0),
        FIT_XY      (1),
        FIT_START   (2),
        FIT_CENTER  (3),
        FIT_END     (4),
        CENTER      (5),
        CENTER_CROP (6),
        CENTER_INSIDE (7),
        TOP_CROP (8);

        ScaleType(int ni) {
            nativeInt = ni;
        }
        final int nativeInt;
    }

    public TopCropImageView(Context context) {
        super(context);
    }

    public TopCropImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TopCropImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setScaleType(ScaleType scaleType) {
        mSkaleType = scaleType;
        if(scaleType != ScaleType.TOP_CROP) {
            super.setScaleType(ImageView.ScaleType.valueOf(scaleType.name()));
        }
        else {
            super.setScaleType(ImageView.ScaleType.MATRIX);
        }
    }

    @Override
    protected boolean setFrame(int l, int t, int r, int b) {
        if(mSkaleType == ScaleType.TOP_CROP) {
            final Matrix matrix = getImageMatrix();
            float scale;
            final int viewWidth = getWidth() - getPaddingLeft() - getPaddingRight();
            final int viewHeight = getHeight() - getPaddingTop() - getPaddingBottom();
            final int drawableWidth = getDrawable().getIntrinsicWidth() - 1;
            final int drawableHeight = getDrawable().getIntrinsicHeight() - 1;
            if (drawableWidth * viewHeight > drawableHeight * viewWidth) {
                scale = (float) viewHeight / (float) drawableHeight;
            } else {
                scale = (float) viewWidth / (float) drawableWidth;
            }
            matrix.setScale(scale, scale);
            setImageMatrix(matrix);
        }
        return super.setFrame(l, t, r, b);
    }
}
