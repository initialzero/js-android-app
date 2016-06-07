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

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);

        alterControlStates();
    }

    @AfterViews
    final void init() {
        currentPageLabel.setText(String.valueOf(getCurrentPage()));
        alterControlStates();
    }

    @Click
    final void firstPage() {
        updateCurrentPage(FIRST_PAGE);
        onPageChangeListener.onPageSelected(getCurrentPage());
    }

    @Click
    final void previousPage() {
        if (getCurrentPage() != FIRST_PAGE) {
            updateCurrentPage(getCurrentPage() - 1);
            onPageChangeListener.onPageSelected(getCurrentPage());
        }
    }

    @Click
    final void nextPage() {
        if (getCurrentPage() != getTotalPages()) {
            updateCurrentPage(getCurrentPage() + 1);
            onPageChangeListener.onPageSelected(getCurrentPage());
        }
    }

    @Click
    final void lastPage() {
        updateCurrentPage(getTotalPages());
        onPageChangeListener.onPageSelected(getCurrentPage());
    }

    @Click(R.id.currentPageLabel)
    final void selectCurrentPage() {
        onOnPickerSelectedListener.onPagePickerRequested();
    }

    @Override
    protected void alterTotalCount() {
        if (isTotalPagesLoaded()) {
            progressLayout.setVisibility(GONE);
            totalPageLabel.setVisibility(VISIBLE);
            totalPageLabel.setText(getContext().getString(R.string.of, getTotalPages()));
            lastPage.setEnabled(isEnabled() && getCurrentPage() != getTotalPages());
        } else {
            progressLayout.setVisibility(VISIBLE);
            totalPageLabel.setVisibility(GONE);
            lastPage.setEnabled(false);
        }
    }

    @Override
    protected void alterControlStates() {
        currentPageLabel.setText(String.valueOf(getCurrentPage()));

        boolean isCurrentPageFirst = getCurrentPage() == FIRST_PAGE;
        boolean isCurrentPageLast = getCurrentPage() == getTotalPages();

        previousPage.setEnabled(isEnabled() && !isCurrentPageFirst);
        firstPage.setEnabled(isEnabled() && !isCurrentPageFirst);
        nextPage.setEnabled(isEnabled() && !isCurrentPageLast);
        lastPage.setEnabled(isEnabled() && !isCurrentPageLast && isTotalPagesLoaded());
        currentPageLabel.setEnabled(isEnabled());
    }

}