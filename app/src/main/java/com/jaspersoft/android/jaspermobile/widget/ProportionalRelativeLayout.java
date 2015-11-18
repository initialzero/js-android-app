package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class ProportionalRelativeLayout extends RelativeLayout {

    private static final double WIDTH_RATIO = 7;
    private static final double HEIGHT_RATIO = 6;
    
    public ProportionalRelativeLayout(Context context) {
        super(context);
    }

    public ProportionalRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProportionalRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
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
