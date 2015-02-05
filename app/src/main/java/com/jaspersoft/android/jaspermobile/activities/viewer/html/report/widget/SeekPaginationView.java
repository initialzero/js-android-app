package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.OnPageSelectedListener;
import com.jaspersoft.android.jaspermobile.dialog.PageDialogFragment;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.SeekBarProgressChange;
import org.androidannotations.annotations.SeekBarTouchStop;
import org.androidannotations.annotations.ViewById;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EViewGroup(R.layout.view_pagination_seekbar)
public class SeekPaginationView extends AbstractPaginationView {

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
    private Toast mToast;

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

    @SuppressLint("ShowToast")
    @AfterViews
    final void init() {
        mToast = Toast.makeText(getContext(), "", Toast.LENGTH_SHORT);
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
        PageDialogFragment.configure()
                .setMin(1)
                .setMax(isTotalPagesLoaded() ? getTotalPages() : Integer.MAX_VALUE)
                .setOnPageSelectedListener(onPageSelectedListener)
                .show(getFragmentManager());
    }

    @Override
    protected void alterControlStates() {
        currentPageLabel.setText(String.valueOf(getCurrentPage()));
        if (isTotalPagesLoaded()) {
            int currentProgress = Math.round((float) getCurrentPage() * 100 / (float) getTotalPages());
            seekBar.setProgress(currentProgress);
        }
    }

    @SeekBarProgressChange(R.id.seekBar)
    final void onProgressChange() {
        mToast.setText(String.valueOf(currentPageFromProgress()));
        mToast.show();
    }

    @SeekBarTouchStop(R.id.seekBar)
    final void onProgressChangeOnSeekBar() {
        setCurrentPage(currentPageFromProgress());
        dispatchChangeListener();
    }

    private int currentPageFromProgress() {
        int progress = seekBar.getProgress();
        int currentPage;

        if (progress == 0) {
            currentPage = FIRST_PAGE;
        } else {
            currentPage = Math.round((float) getTotalPages() * (float) progress / 100);
        }
        if (currentPage == 0) {
            currentPage = FIRST_PAGE;
        }
        return currentPage;
    }
}
