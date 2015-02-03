package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.NumberDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.OnPageSelectedListener;
import com.jaspersoft.android.jaspermobile.dialog.PageDialogFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EViewGroup(R.layout.pagination_bar_layout)
public class PaginationBarView extends RelativeLayout {
    private static final int FIRST_PAGE = 1;
    private static final String INSTANCE_STATE = "INSTANCE_STATE";
    private static final String CURRENT_PAGE = "CURRENT_PAGE";
    private static final String TOTAL_PAGE = "TOTAL_PAGE";

    @ViewById
    protected TextView firstPage;
    @ViewById
    protected TextView nextPage;
    @ViewById
    protected TextView previousPage;
    @ViewById
    protected TextView lastPage;

    @ViewById
    protected TextView currentPageLabel;
    @ViewById
    protected TextView totalPageLabel;
    @ViewById
    protected View progressLayout;

    private OnPageChangeListener onPageChangeListener;
    private OnVisibilityChangeListener onVisibilityChangeListener;
    private int currentPage = FIRST_PAGE;
    private int mTotalPage = -1;

    private final OnPageSelectedListener onPageSelectedListener =
            new OnPageSelectedListener() {
                @Override
                public void onPageSelected(int page) {
                    currentPage = page;
                    PaginationBarView.this.onPageSelected();
                }
            };

    public PaginationBarView(Context context) {
        super(context);
    }

    public PaginationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PaginationBarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PaginationBarView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @AfterViews
    final void init() {
        currentPageLabel.setText(String.valueOf(currentPage));
        alterControlStates();
    }

    public void setOnVisibilityChangeListener(OnVisibilityChangeListener onVisibilityChangeListener) {
        this.onVisibilityChangeListener = onVisibilityChangeListener;
    }

    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.onPageChangeListener = onPageChangeListener;
    }

    public void setVisible(boolean flag) {
        setVisibility(flag ? VISIBLE : GONE);
        if (onVisibilityChangeListener != null) {
            onVisibilityChangeListener.onVisibilityChanged(flag);
        }
    }

    public void showTotalCount(int totalPage) {
        mTotalPage = totalPage;

        progressLayout.setVisibility(View.GONE);
        totalPageLabel.setVisibility(View.VISIBLE);
        lastPage.setEnabled(true);

        totalPageLabel.setText(getContext().getString(R.string.of, totalPage));
    }

    public void setPage(int page) {
        currentPage = page;
        alterControlStates();
    }

    public int getPage() {
        return currentPage;
    }

    public void navigateTo(int page) {
        currentPage = page;
        onPageSelected();
    }

    public boolean hasTotalCount() {
        return (mTotalPage != -1);
    }

    @Click
    final void firstPage() {
        currentPage = FIRST_PAGE;
        onPageSelected();
    }

    @Click
    final void previousPage() {
        if (currentPage != FIRST_PAGE) {
            currentPage -= 1;
        }
        onPreviousPage();
    }

    @Click
    final void nextPage() {
        if (currentPage != mTotalPage) {
            currentPage += 1;
        }
        onNextPage();
    }

    @Click
    final void lastPage() {
        currentPage = mTotalPage;
        onPageSelected();
    }

    @Click(R.id.currentPageLabel)
    final void selectCurrentPage() {
        boolean totalPagesLoaded = mTotalPage != 0;
        if (totalPagesLoaded) {
            NumberDialogFragment.builder(getFragmentManager())
                    .selectListener(onPageSelectedListener)
                    .value(currentPage)
                    .minValue(1)
                    .maxValue(mTotalPage)
                    .show();
        } else {
            PageDialogFragment.show(getFragmentManager(), onPageSelectedListener);
        }
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        Bundle bundle = (Bundle) state;
        currentPage = bundle.getInt(CURRENT_PAGE);
        mTotalPage = bundle.getInt(TOTAL_PAGE);
        Parcelable instanceState = bundle.getParcelable(INSTANCE_STATE);
        super.onRestoreInstanceState(instanceState);
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        super.onSaveInstanceState();
        Bundle bundle = new Bundle();
        bundle.putParcelable(INSTANCE_STATE, super.onSaveInstanceState());
        bundle.putInt(CURRENT_PAGE, currentPage);
        bundle.putInt(TOTAL_PAGE, mTotalPage);
        return bundle;
    }

    private FragmentManager getFragmentManager() {
        FragmentActivity activity = (FragmentActivity) getContext();
        return activity.getSupportFragmentManager();
    }

    private void onNextPage() {
        alterControlStates();
        if (onPageChangeListener != null) {
            onPageChangeListener.onNextPage(currentPage);
        }
    }

    private void onPreviousPage() {
        alterControlStates();
        if (onPageChangeListener != null) {
            onPageChangeListener.onPreviousPage(currentPage);
        }
    }

    private void onPageSelected() {
        alterControlStates();
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageSelected(currentPage);
        }
    }

    private void alterControlStates() {
        boolean hasTotalCount = (mTotalPage > 0);
        currentPageLabel.setText(String.valueOf(currentPage));

        if (currentPage == mTotalPage) {
            previousPage.setEnabled(true);
            firstPage.setEnabled(true);
            nextPage.setEnabled(false);
            lastPage.setEnabled(!hasTotalCount);
            return;
        }
        if (currentPage == FIRST_PAGE) {
            previousPage.setEnabled(false);
            firstPage.setEnabled(false);
            nextPage.setEnabled(true);
            lastPage.setEnabled(hasTotalCount);
            return;
        }
        previousPage.setEnabled(true);
        firstPage.setEnabled(true);
        nextPage.setEnabled(true);
        lastPage.setEnabled(hasTotalCount);
    }

    public static interface OnVisibilityChangeListener {
        void onVisibilityChanged(boolean visible);
    }

    public static interface OnPageChangeListener {
        void onNextPage(int currentPage);
        void onPreviousPage(int currentPage);
        void onPageSelected(int currentPage);
    }

}
