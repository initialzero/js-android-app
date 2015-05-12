package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EViewGroup(R.layout.view_pagination_bar)
public class PaginationBarView extends AbstractPaginationView {

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
        currentPageLabel.setText(String.valueOf(getCurrentPage()));
        alterControlStates();
    }

    @Click
    final void firstPage() {
        setCurrentPage(FIRST_PAGE);
        onPageChangeListener.onPageSelected(getCurrentPage());
    }

    @Click
    final void previousPage() {
        if (getCurrentPage() != FIRST_PAGE) {
            setCurrentPage(getCurrentPage() - 1);
            onPageChangeListener.onPageSelected(getCurrentPage());
        }
    }

    @Click
    final void nextPage() {
        if (getCurrentPage() != getTotalPages()) {
            setCurrentPage(getCurrentPage() + 1);
            onPageChangeListener.onPageSelected(getCurrentPage());
        }
    }

    @Click
    final void lastPage() {
        setCurrentPage(getTotalPages());
        onPageChangeListener.onPageSelected(getCurrentPage());
    }

    @Click(R.id.currentPageLabel)
    final void selectCurrentPage() {
        onPageChangeListener.onPagePickerRequested();
    }

    @Override
    protected void alterTotalCount() {
        if(getTotalPages() != -1) {
            progressLayout.setVisibility(View.GONE);
            totalPageLabel.setVisibility(View.VISIBLE);
            totalPageLabel.setText(getContext().getString(R.string.of, getTotalPages()));
            lastPage.setEnabled(true);
        }
        else {
            progressLayout.setVisibility(View.VISIBLE);
            totalPageLabel.setVisibility(View.GONE);
            lastPage.setEnabled(false);
        }
    }

    @Override
    protected void alterControlStates() {
        currentPageLabel.setText(String.valueOf(getCurrentPage()));

        if (getCurrentPage() == getTotalPages()) {
            previousPage.setEnabled(true);
            firstPage.setEnabled(true);
            nextPage.setEnabled(false);
            lastPage.setEnabled(!isTotalPagesLoaded());
            return;
        }
        if (getCurrentPage() == FIRST_PAGE) {
            previousPage.setEnabled(false);
            firstPage.setEnabled(false);
            nextPage.setEnabled(true);
            lastPage.setEnabled(isTotalPagesLoaded());
            return;
        }
        previousPage.setEnabled(true);
        firstPage.setEnabled(true);
        nextPage.setEnabled(true);
        lastPage.setEnabled(isTotalPagesLoaded());
    }

}
