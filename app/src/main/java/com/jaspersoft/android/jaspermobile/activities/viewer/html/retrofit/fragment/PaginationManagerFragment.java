/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html.retrofit.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.View;
import android.widget.TextView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.dialog.NumberDialogFragment;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.Map;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.pagination_bar_layout)
public class PaginationManagerFragment extends RoboSpiceFragment {

    public static final String TAG = PaginationManagerFragment.class.getSimpleName();
    private static final int FIRST_PAGE = 1;

    @Inject
    JsRestClient jsRestClient;

    @ViewById
    TextView firstPage;
    @ViewById
    TextView previousPage;
    @ViewById
    TextView nextPage;
    @ViewById
    TextView lastPage;
    @ViewById
    View rootContainer;

    @ViewById
    TextView currentPageLabel;
    @ViewById
    TextView totalPageLabel;

    @InstanceState
    int totalPage;
    @InstanceState
    String requestId;

    @InstanceState
    int currentPage = FIRST_PAGE;

    private final Map<Integer, NodeWebViewFragment> pagesMap = Maps.newHashMap();
    private PagesAdapter mAdapter;

    @AfterViews
    final void init() {
        mAdapter = new PagesAdapter(getFragmentManager());

        totalPageLabel.setVisibility(View.INVISIBLE);
        lastPage.setVisibility(View.INVISIBLE);

        currentPageLabel.setText(String.valueOf(currentPage));
        alterControlStates();
    }

    public void showTotalPageCount(int totalPage) {
        if (totalPage > 1) {
            totalPageLabel.setVisibility(View.VISIBLE);
            lastPage.setVisibility(View.VISIBLE);

            totalPageLabel.setText(getString(R.string.of, totalPage));
        }
    }

    @Click
    final void firstPage() {
        currentPage = FIRST_PAGE;
        paginateToCurrentSelection();
    }

    @Click
    final void previousPage() {
        if (currentPage != FIRST_PAGE) {
            currentPage -= 1;
        }
        paginateToCurrentSelection();
    }

    @Click
    final void nextPage() {
        if (currentPage != totalPage) {
            currentPage += 1;
        }
        paginateToCurrentSelection();
    }

    @Click
    final void lastPage() {
        currentPage = totalPage;
        paginateToCurrentSelection();
    }

    @Click(R.id.currentPageLabel)
    final void selectCurrentPage() {
        if (totalPage != 0) {
            NumberDialogFragment.show(getFragmentManager(), currentPage, totalPage,
                    new NumberDialogFragment.OnPageSelectedListener() {
                        @Override
                        public void onPageSelected(int page) {
                            currentPage = page;
                            alterControlStates();
                        }
                    });
        }
    }

    public void paginateToCurrentSelection() {
        if (rootContainer.getVisibility() == View.GONE) {
            rootContainer.setVisibility(View.VISIBLE);
        }

        alterControlStates();

        NodeWebViewFragment nodeWebViewFragment;
        if (mAdapter.getCount() == 0) {
            nodeWebViewFragment = getNodeWebViewFragment();
        } else {
            if (pagesMap.containsKey(currentPage)) {
                nodeWebViewFragment = pagesMap.get(currentPage);
            } else {
                nodeWebViewFragment = getNodeWebViewFragment();
            }
        }

        getFragmentManager().beginTransaction()
                .replace(R.id.content, nodeWebViewFragment,
                        NodeWebViewFragment.TAG + currentPage).commit();
    }

    private NodeWebViewFragment getNodeWebViewFragment() {
        NodeWebViewFragment nodeWebViewFragment =
                NodeWebViewFragment_.builder().requestId(requestId).page(currentPage).build();
        pagesMap.put(currentPage, nodeWebViewFragment);
        return nodeWebViewFragment;
    }

    private void alterControlStates() {
        currentPageLabel.setText(String.valueOf(currentPage));

        if (currentPage == totalPage) {
            previousPage.setEnabled(true);
            firstPage.setEnabled(true);
            nextPage.setEnabled(false);
            lastPage.setEnabled(false);
            return;
        }
        if (currentPage == FIRST_PAGE) {
            previousPage.setEnabled(false);
            firstPage.setEnabled(false);
            nextPage.setEnabled(true);
            lastPage.setEnabled(true);
            return;
        }
        previousPage.setEnabled(true);
        firstPage.setEnabled(true);
        nextPage.setEnabled(true);
        lastPage.setEnabled(true);
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    public class PagesAdapter extends FragmentPagerAdapter {
        public PagesAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return pagesMap.keySet().size();
        }

        @Override
        public Fragment getItem(int position) {
            return Lists.newLinkedList(pagesMap.values()).get(position);
        }
    }

}
