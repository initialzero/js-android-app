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

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Queues;
import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.ExportOutputData;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.ReportExportOutputLoader;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.RequestExecutor;
import com.jaspersoft.android.jaspermobile.dialog.NumberDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.OnPageSelectedListener;
import com.jaspersoft.android.jaspermobile.dialog.PageDialogFragment;
import com.jaspersoft.android.jaspermobile.network.UniversalRequestListener;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.ReportDetailsRequest;
import com.jaspersoft.android.sdk.client.oxm.report.ReportExecutionResponse;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Set;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.pagination_bar_layout)
public class PaginationManagerFragment extends RoboSpiceFragment {

    public static final String TAG = PaginationManagerFragment.class.getSimpleName();
    private static final int FIRST_PAGE = 1;

    @FragmentArg
    double versionCode;

    @Inject
    @Named("MAX_PAGE_ALLOWED")
    private int maxPageAllowed;
    @Inject
    JsRestClient jsRestClient;
    @InstanceState
    boolean mReportIsEmpty;

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
    View paginationLayout;
    @ViewById
    View progressLayout;

    @ViewById
    TextView currentPageLabel;
    @ViewById
    TextView totalPageLabel;

    @InstanceState
    int mTotalPage;
    @InstanceState
    String requestId;

    @InstanceState
    int currentPage = FIRST_PAGE;

    private final OnPageSelectedListener onPageSelectedListener =
            new OnPageSelectedListener() {
                @Override
                public void onPageSelected(int page) {
                    currentPage = page;
                    paginateToCurrentSelection();
                }
            };

    private final Comparator<NodeWebViewFragment> pageComparator =
            new Comparator<NodeWebViewFragment>() {
                @Override
                public int compare(NodeWebViewFragment leftFragment, NodeWebViewFragment rightFragment) {
                    int leftPage = NodeWebViewFragment.getPage(leftFragment);
                    int rightPage = NodeWebViewFragment.getPage(rightFragment);
                    return Ints.compare(leftPage, rightPage);
                }
            };

    private final List<NodeWebViewFragment> fragments = Lists.newArrayList();
    private final Set<Integer> pages = Sets.newHashSet();
    private final Deque<Integer> stack = Queues.newArrayDeque();

    @AfterViews
    final void init() {
        currentPageLabel.setText(String.valueOf(currentPage));
        alterControlStates();

        if (mTotalPage != 0) {
            showTotalPageCount(mTotalPage);
        }
    }

    public void showTotalPageCount(int totalPage) {
        mTotalPage = totalPage;

        progressLayout.setVisibility(View.GONE);
        totalPageLabel.setVisibility(View.VISIBLE);
        lastPage.setEnabled(true);

        totalPageLabel.setText(getString(R.string.of, totalPage));
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
        if (currentPage != mTotalPage) {
            currentPage += 1;
        }
        paginateToCurrentSelection();
    }

    @Click
    final void lastPage() {
        currentPage = mTotalPage;
        paginateToCurrentSelection();
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

    public void loadNextPageInBackground() {
        loadReportByPage(currentPage + 1, RequestExecutor.Mode.SILENT);
    }

    public void setPaginationControlVisible(boolean visible) {
        rootContainer.setVisibility(visible ? View.VISIBLE : View.GONE);

        RelativeLayout htmlViewer = (RelativeLayout)
                getActivity().findViewById(R.id.htmlViewer_layout);
        if (htmlViewer != null) {
            htmlViewer.setPadding(0, 0, 0, visible ? paginationLayout.getHeight() : 0);
        }
    }

    public void paginateTo(int page) {
        currentPage = page;
        paginateToCurrentSelection();
    }

    public void paginateToCurrentSelection() {
        updateStack();
        alterControlStates();

        boolean noPagesLoaded = fragments.isEmpty();
        boolean thereIsNoSuchPageForIndex = !pages.contains(currentPage);

        if (noPagesLoaded || thereIsNoSuchPageForIndex) {
            loadReportByPage(currentPage);
        } else {
            Optional<NodeWebViewFragment> optional = findFragmentByPage(currentPage);
            if (optional.isPresent()) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.content, optional.get(),
                                NodeWebViewFragment.TAG + currentPage).commit();
                updatePageForNewRequestId();
            }
        }
    }

    public void update() {
        if (!isPaginationLoaded()) {
            ReportDetailsRequest reportDetailsRequest = new ReportDetailsRequest(jsRestClient, requestId);
            UniversalRequestListener<ReportExecutionResponse> universalRequestListener =
                    UniversalRequestListener.builder(getActivity())
                            .semanticListener(new ReportDetailsRequestListener())
                            .create();
            getSpiceManager().execute(reportDetailsRequest, universalRequestListener);
        }

        Optional<NodeWebViewFragment> optional = getCurrentNodeWebViewFragment();
        if (optional.isPresent()) {
            optional.get().loadFinalOutput();
        }
    }

    public boolean isPaginationLoaded() {
        return (mTotalPage != 0);
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getRequestId() {
        return requestId;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void updatePageForNewRequestId() {
        Optional<NodeWebViewFragment> optional = getCurrentNodeWebViewFragment();
        if (optional.isPresent() && optional.get().needUpdate()) {
            optional.get().refreshForNewRequestId();
        }
    }

    private void loadReportByPage(int page) {
        loadReportByPage(page, RequestExecutor.Mode.VISIBLE);
    }

    private void loadReportByPage(int page, RequestExecutor.Mode executionMode) {
        pages.add(page);

        ReportExportOutputLoader.builder()
                .setControlFragment(this)
                .setExecutionMode(executionMode)
                .setJSRestClient(jsRestClient)
                .setRequestId(requestId)
                .setVersionCode(versionCode)
                .setResultListener(new ExportResultListener(executionMode, page))
                .create()
                .loadByPage(page);
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

    @NonNull
    private ReportExecutionFragment getReportExecutionFragment() {
        return (ReportExecutionFragment)
                getFragmentManager().findFragmentByTag(ReportExecutionFragment.TAG);
    }

    @NonNull
    private Optional<NodeWebViewFragment> getCurrentNodeWebViewFragment() {
        return findFragmentByPage(currentPage);
    }

    @NonNull
    private Optional<NodeWebViewFragment> findFragmentByPage(final int page) {
        return Iterables.tryFind(fragments, new Predicate<NodeWebViewFragment>() {
            public boolean apply(NodeWebViewFragment fragment) {
                return NodeWebViewFragment.getPage(fragment) == page;
            }
        });
    }

    @NonNull
    private FilterManagerFragment getFilterMangerFragment() {
        return (FilterManagerFragment)
                getFragmentManager().findFragmentByTag(FilterManagerFragment.TAG);
    }

    /**
     * This dirty way to fix memory issue on report viewer section.
     * We are basically keeping only few instances of Fragments in memory.
     * This is also respects bidirectional behavior pagination has.
     */
    private void removeOutdatedPageOnDemand() {
        if (fragments.size() > maxPageAllowed) {
            removeCachedPage();
        }
    }

    private void removeCachedPage() {
        int max = Collections.max(stack);
        // User can navigate back/forward this involves appropriate 'poll' events to be invoked.
        // If user has in cache [3, 2, 1] pages and loads 4 then result will be [4, 3, 2].
        // If user has in cache [4, 3, 2] pages and loads 1 then result will be [3, 2, 1].
        // This makes bidirectional removal experience.
        int pageToDelete = (currentPage >= max) ? stack.pollLast() : stack.pollFirst();
        fragments.remove(findFragmentByPage(pageToDelete).get());
        pages.remove(pageToDelete);
    }

    private void updateStack() {
        if (stack.contains(currentPage)) {
            return;
        }
        if (stack.isEmpty()) {
            stack.addFirst(currentPage);
        } else {
            int max = Collections.max(stack);
            // User can navigate back/forward this involves appropriate 'push' events to be invoked.
            // If user has in cache [3, 2, 1] pages and loads 4 then result will be [4, 3, 2, 1].
            // If user has in cache [4, 3, 2] pages and loads 1 then result will be [3, 2, 1].
            // This makes bidirectional insertion experience.
            if (currentPage > max)  {
                stack.addFirst(currentPage);
            } else {
                stack.addLast(currentPage);
            }
        }
    }

    //---------------------------------------------------------------------
    // Inner classes
    //---------------------------------------------------------------------

    private class ExportResultListener implements ReportExportOutputLoader.ResultListener {
        private final RequestExecutor.Mode executionMode;
        private final int outputPage;

        private ExportResultListener(RequestExecutor.Mode executionMode, int outputPage) {
            this.executionMode = executionMode;
            this.outputPage = outputPage;
        }

        @Override
        public void onFailure() {
            pages.remove(outputPage);
        }

        @Override
        public void onSuccess(ExportOutputData output) {
            NodeWebViewFragment nodeWebViewFragment =
                    NodeWebViewFragment_.builder()
                            .requestId(requestId)
                            .executionId(output.getExecutionId())
                            .currentHtml(output.getData())
                            .outputFinal(output.isFinal())
                            .versionCode(versionCode)
                            .page(outputPage)
                            .build();
            fragments.add(nodeWebViewFragment);
            Collections.sort(fragments, pageComparator);

            // This means that we have 2 page loaded
            // and that is enough to show pagination control
            if (outputPage == 2) {
                setPaginationControlVisible(true);
            }
            if (executionMode == RequestExecutor.Mode.VISIBLE) {
                getFragmentManager().beginTransaction()
                        .replace(R.id.content, nodeWebViewFragment,
                                NodeWebViewFragment.TAG + currentPage).commit();
            }
            removeOutdatedPageOnDemand();
        }
    }

    private class ReportDetailsRequestListener extends UniversalRequestListener.SimpleSemanticListener<ReportExecutionResponse> {
        @Override
        public final void onSemanticSuccess(ReportExecutionResponse response) {
            int totalPageCount = response.getTotalPages();
            boolean needToShow = (totalPageCount > 1);
            setPaginationControlVisible(needToShow);

            if (needToShow) {
                showTotalPageCount(response.getTotalPages());
            }

            if (totalPageCount == 0) {
                getReportExecutionFragment().showEmptyReportOptionsDialog();
            } else {
                getFilterMangerFragment().makeSnapshot();
            }
        }
    }
}
