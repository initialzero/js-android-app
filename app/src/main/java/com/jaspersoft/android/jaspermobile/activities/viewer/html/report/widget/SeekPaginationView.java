package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.NumberDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.OnPageSelectedListener;
import com.jaspersoft.android.jaspermobile.dialog.PageDialogFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.SeekBarTouchStop;
import org.androidannotations.annotations.ViewById;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EViewGroup(R.layout.view_pagination_seekbar)
public class SeekPaginationView extends AbstractPaginationView {
    private static final String TAG = SeekPaginationView.class.getSimpleName();

    @ViewById
    protected SeekBar seekBar;
    @ViewById
    protected TextView currentPageLabel;
    @ViewById
    protected TextView totalPageLabel;
    @ViewById
    protected View progressLayout;

    private final OnPageSelectedListener onPageSelectedListener =
            new OnPageSelectedListener() {
                @Override
                public void onPageSelected(int page) {
                    setCurrentPage(page);
                    dispatchChangeListener();
                }
            };

    public SeekPaginationView(Context context) {
        super(context);
    }

    public SeekPaginationView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SeekPaginationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public SeekPaginationView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @AfterViews
    final void init() {
        Timber.tag(TAG);
    }

    @Override
    protected void alterTotalCount() {
        seekBar.setEnabled(true);
        progressLayout.setVisibility(View.GONE);
        totalPageLabel.setVisibility(View.VISIBLE);

        totalPageLabel.setText(getContext().getString(R.string.of, getTotalPages()));
    }

    @Click(R.id.currentPageLabel)
    final void selectCurrentPage() {
        if (isTotalPagesLoaded()) {
            NumberDialogFragment.builder(getFragmentManager())
                    .selectListener(onPageSelectedListener)
                    .value(getCurrentPage())
                    .minValue(1)
                    .maxValue(getTotalPages())
                    .show();
        } else {
            PageDialogFragment.show(getFragmentManager(), onPageSelectedListener);
        }
    }

    @Override
    protected void alterControlStates() {
        currentPageLabel.setText(String.valueOf(getCurrentPage()));
    }

    @SeekBarTouchStop(R.id.seekBar)
    void onProgressChangeOnSeekBar(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        int currentPage;

        if (progress == 0) {
            currentPage = FIRST_PAGE;
        } else  {
            currentPage = getTotalPages() * progress / 100;
        }
        setCurrentPage(currentPage);
        dispatchChangeListener();
    }
}
