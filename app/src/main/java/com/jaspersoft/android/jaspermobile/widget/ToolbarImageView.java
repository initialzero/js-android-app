package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.util.AttributeSet;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class ToolbarImageView extends TopCropImageView {

    private static final double WIDTH_RATIO = 3;
    private static final double HEIGHT_RATIO = 2;

    public ToolbarImageView(Context context) {
        super(context);
    }

    public ToolbarImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ToolbarImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int heightSize = (int) (HEIGHT_RATIO / WIDTH_RATIO * widthSize);

        int newHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        super.onMeasure(widthMeasureSpec, newHeightSpec);
    }
}
