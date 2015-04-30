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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.FragmentCreator;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.NodePagerAdapter;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.ReportSession;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget.AbstractPaginationView;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget.PaginationBarView;
import com.jaspersoft.android.jaspermobile.dialog.NumberDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.PageDialogFragment;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.widget.JSViewPager;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;
import org.springframework.http.HttpStatus;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.fragment_pagination_manager)
public class PaginationManagerFragment extends RoboSpiceFragment implements NumberDialogFragment.NumberDialogClickListener, PageDialogFragment.PageDialogClickListener {

    public static final String TAG = PaginationManagerFragment.class.getSimpleName();

    @Inject
    private JsRestClient jsRestClient;

    @ViewById
    protected View rootContainer;
    @ViewById
    protected AbstractPaginationView paginationControl;

    @InstanceState
    protected int mTotalPage;

    @Bean
    protected ReportSession reportSession;

    private JSViewPager viewPager;
    private NodePagerAdapter mAdapter;

    @AfterViews
    final void init() {
        reportSession.registerObserver(sessionObserver);

        viewPager = (JSViewPager) getActivity().findViewById(R.id.viewPager);
        viewPager.setSwipeable(false);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                int currentPage = position + 1;
                paginationControl.setCurrentPage(currentPage);

                boolean showNext = (currentPage == mAdapter.getCount());
                if (paginationControl.isTotalPagesLoaded()) {
                    int totalPages = paginationControl.getTotalPages();
                    showNext &= (currentPage + 1 <= totalPages);
                }

                if (showNext) {
                    viewPager.setOnPageChangeListener(null);
                    mAdapter.addPage();
                    mAdapter.notifyDataSetChanged();
                    viewPager.setOnPageChangeListener(this);
                }
            }
        });

        mAdapter = new NodePagerAdapter(getFragmentManager(), new FragmentCreator<Fragment, Integer>() {
            @Override
            public Fragment createFragment(Integer page) {
                NodeWebViewFragment nodeWebViewFragment =
                        NodeWebViewFragment_.builder()
                                .page(page)
                                .build();
                nodeWebViewFragment.setOnPageLoadListener(nodeListener);
                return nodeWebViewFragment;
            }
        });
        viewPager.setAdapter(mAdapter);

        paginationControl.setOnPageChangeListener(new PaginationBarView.OnPageChangeListener() {
            @Override
            public void onPageSelected(final int page) {
                changePage(page);
            }

            @Override
            public void onPagePickerRequested() {
                if (paginationControl.isTotalPagesLoaded()) {
                    NumberDialogFragment.createBuilder(getFragmentManager())
                            .setMinValue(1)
                            .setCurrentValue(paginationControl.getCurrentPage())
                            .setMaxValue(paginationControl.getTotalPages())
                            .setTargetFragment(PaginationManagerFragment.this)
                            .show();
                } else {
                    PageDialogFragment.createBuilder(getFragmentManager())
                            .setMaxValue(Integer.MAX_VALUE)
                            .setTargetFragment(PaginationManagerFragment.this)
                            .show();
                }
            }
        });

        if (mTotalPage != 0) {
            paginationControl.setTotalCount(mTotalPage);
            showPaginationControl();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        reportSession.removeObserver(sessionObserver);
    }

    public void paginateToCurrentSelection() {
        viewPager.setCurrentItem(paginationControl.getCurrentPage() - 1);
    }

    public void paginateTo(int page) {
        int maximumAllowed = mAdapter.getCount();
        if (page <= maximumAllowed) {
            viewPager.setCurrentItem(page - 1);
        }
    }

    public void showTotalPageCount(int totalPageCount) {
        paginationControl.setTotalCount(totalPageCount);
    }

    public void loadNextPageInBackground() {
        mAdapter.addPage();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPageSelected(int page, int requestCode) {
        paginationControl.setCurrentPage(page);
        changePage(page);
    }

    @Override
    public void onPageSelected(int page) {
        paginationControl.setCurrentPage(page);
        changePage(page);
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void showPaginationControl() {
        rootContainer.setVisibility(View.VISIBLE);

        RelativeLayout htmlViewer = (RelativeLayout)
                getActivity().findViewById(R.id.htmlViewer_layout);
        if (htmlViewer != null) {
            htmlViewer.setPadding(0, 0, 0, paginationControl.getHeight());
        }
    }

    private void changePage(int page) {
        int count = mAdapter.getCount();
        int item = page - 1;
        if (count < page) {
            mAdapter.setCount(page);
            mAdapter.notifyDataSetChanged();
        }
        viewPager.setCurrentItem(item);
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private final ReportSession.ExecutionObserver sessionObserver =
            new ReportSession.ExecutionObserver() {
                @Override
                public void onRequestIdChanged(String requestId) {
                    mAdapter.clear();
                    mAdapter.addPage();
                    mAdapter.notifyDataSetChanged();
                    paginationControl.setCurrentPage(1);
                    viewPager.setCurrentItem(0);
                }

                @Override
                public void onPagesLoaded(int totalPage) {
                    mTotalPage = totalPage;
                    paginationControl.setTotalCount(mTotalPage);
                    if (totalPage > 1) {
                        showTotalPageCount(totalPage);
                    }
                }
            };

    private final NodeWebViewFragment.OnPageLoadListener nodeListener =
            new NodeWebViewFragment.OnPageLoadListener() {
                @Override
                public void onFailure(Exception exception) {
                    int statusCode = RequestExceptionHandler.extractStatusCode(exception);
                    if (statusCode != 0 && statusCode == HttpStatus.BAD_REQUEST.value()) {
                        // Enforcing max page or first one
                        // this situation possible due to the user has entered out of range page
                        mAdapter.setCount(paginationControl.isTotalPagesLoaded() ?
                                paginationControl.getTotalPages() : 2);
                        mAdapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onSuccess(int page) {
                    // This means that we have 2 page loaded
                    // and that is enough to show pagination control
                    if (page == 2) {
                        showPaginationControl();
                    }
                }
            };
}
