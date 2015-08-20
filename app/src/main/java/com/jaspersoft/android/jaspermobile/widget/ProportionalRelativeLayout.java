package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class ProportionalRelativeLayout extends RelativeLayout {

    private float widthToHeightRatio;
    private float widthPercent;
    private float heightPercent;

    public ProportionalRelativeLayout(Context context) {
        super(context);

        widthPercent = 1;
        heightPercent = 1;
    }

    public ProportionalRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ProportionalRelativeLayout, 0, 0);
        widthPercent = a.getFloat(R.styleable.ProportionalRelativeLayout_widthPercent, 1);
        heightPercent = a.getFloat(R.styleable.ProportionalRelativeLayout_heightPercent, 1);
        widthToHeightRatio = a.getFloat(R.styleable.ProportionalRelativeLayout_widthToHeightRatio, 0);

        a.recycle();
    }

    public ProportionalRelativeLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        widthPercent = 1;
        heightPercent = 1;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int newHeightSpec;
        int newWidthSpec;

        if (widthToHeightRatio != 0) {
            int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            int heightSize = (int) (widthToHeightRatio * widthSize);

            newWidthSpec = widthMeasureSpec;
            newHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        } else {
            int widthSize = (int) (MeasureSpec.getSize(widthMeasureSpec) * widthPercent);
            int heightSize = (int) (MeasureSpec.getSize(heightMeasureSpec) * heightPercent);

            newWidthSpec = MeasureSpec.makeMeasureSpec(widthSize, MeasureSpec.EXACTLY);
            newHeightSpec = MeasureSpec.makeMeasureSpec(heightSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(newWidthSpec, newHeightSpec);
    }
}
