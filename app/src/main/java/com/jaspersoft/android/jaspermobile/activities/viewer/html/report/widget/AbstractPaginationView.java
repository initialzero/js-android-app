package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public abstract class AbstractPaginationView extends RelativeLayout {
    protected static final int FIRST_PAGE = 1;

    private static final String INSTANCE_STATE = "INSTANCE_STATE";
    private static final String CURRENT_PAGE = "CURRENT_PAGE";
    private static final String TOTAL_PAGE = "TOTAL_PAGE";

    private OnPageChangeListener onPageChangeListener;

    private int currentPage = FIRST_PAGE;
    private int mTotalPages;

    public AbstractPaginationView(Context context) {
        super(context);
    }

    public AbstractPaginationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AbstractPaginationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public AbstractPaginationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public void setCurrentPage(int page) {
        currentPage = page;
        alterControlStates();
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setTotalCount(int totalPage) {
        mTotalPages = totalPage;
        alterTotalCount();
    }

    public boolean isTotalPagesLoaded() {
        return (mTotalPages != 0);
    }

    public int getTotalPages() {
        return mTotalPages;
    }

    protected void dispatchChangeListener() {
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageSelected(currentPage);
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        currentPage = bundle.getInt(CURRENT_PAGE);
        mTotalPages = bundle.getInt(TOTAL_PAGE);
        Parcelable instanceState = bundle.getParcelable(INSTANCE_STATE);
        super.onRestoreInstanceState(instanceState);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(CURRENT_PAGE, currentPage);
        bundle.putInt(TOTAL_PAGE, mTotalPages);
        return bundle;
    }

    /**
     * Method responsible for refreshing data inside associated view.
     * Total page view represents maximum page count for multi page report.
     */
    protected abstract void alterTotalCount();

    /**
     * Method responsible for refreshing data inside control views.
     * Though can be next page, previous page, last and first.
     * In general depends on implementation of corresponding swipe component.
     */
    protected abstract void alterControlStates();

    public static interface OnPageChangeListener {
        void onPageSelected(int currentPage);
    }
}
