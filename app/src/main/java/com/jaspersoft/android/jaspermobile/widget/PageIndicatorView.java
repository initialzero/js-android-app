/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;


/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class PageIndicatorView extends View {

    private Paint mIndicatorPaint;
    private final int mPageCount = 4;
    private int mIndicatorWidth;
    private Rect mIndicatorRect;

    public PageIndicatorView(Context context) {
        super(context);
        prepareIndicatorView(context);
    }

    public PageIndicatorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        prepareIndicatorView(context);
    }

    public PageIndicatorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        prepareIndicatorView(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawRect(mIndicatorRect, mIndicatorPaint);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mIndicatorRect.bottom = getHeight();
        invalidate();
    }

    private void prepareIndicatorView(Context context){
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        mIndicatorWidth = size.x / mPageCount;

        mIndicatorPaint = new Paint();
        mIndicatorPaint.setStyle(Paint.Style.FILL);

        mIndicatorRect = new Rect(0, 0, mIndicatorWidth, 0);

        setPagePosition(0, 0);
    }

    private int getPageIndicatorColour(int pageIndex, float pageOffset){
        int rLeft;
        int gLeft;
        int bLeft;

        int rRight;
        int gRight;
        int bRight;

        switch (pageIndex) {
            case 0:
                rLeft = 7; gLeft = 58; bLeft = 93;
                rRight = 79; gRight = 160; bRight = 24;
                break;
            case 1:
                rLeft = 79; gLeft = 160; bLeft = 24;
                rRight = 236; gRight = 121; bRight = 19;
                break;
            case 2:
                rLeft = 236; gLeft = 121; bLeft = 19;
                rRight = 74; gRight = 74; bRight = 150;
                break;
            case 3:
                rLeft = 74; gLeft = 74; bLeft = 150;
                rRight = 74; gRight = 74; bRight = 150;
                break;
            default:
                rLeft = 7; gLeft = 58; bLeft = 93;
                rRight = 79; gRight = 160; bRight = 24;
                break;
        }

        int rGradient = rLeft + (int) ((rRight - rLeft) * pageOffset);
        int gGradient = gLeft + (int) ((gRight - gLeft) * pageOffset);
        int bGradient = bLeft + (int) ((bRight - bLeft) * pageOffset);
        return Color.rgb(rGradient, gGradient, bGradient);
    }

    public void setPagePosition(int pageIndex, float offset) {
        if(pageIndex < 0 || pageIndex > mPageCount)
            return;

        int pageOffset = (int) (offset * mIndicatorWidth);
        mIndicatorRect.left = mIndicatorWidth * pageIndex + pageOffset;
        mIndicatorRect.right = mIndicatorWidth * pageIndex + mIndicatorWidth + pageOffset;

        mIndicatorPaint.setColor(getPageIndicatorColour(pageIndex, offset));

        invalidate();
    }
}
