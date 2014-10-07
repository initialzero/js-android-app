package com.jaspersoft.android.jaspermobile.activities.report.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.fragment.WebViewFragment;
import com.jaspersoft.android.jaspermobile.dialog.NumberDialogFragment;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.net.URI;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment(R.layout.pagination_bar_layout)
public class PaginationFragment extends RoboSpiceFragment {

    private static final int FIRST_PAGE = 1;
    @ViewById
    TextView firstPage;
    @ViewById
    TextView previousPage;
    @ViewById
    TextView nextPage;
    @ViewById
    TextView lastPage;

    @ViewById
    TextView currentPageLabel;
    @ViewById
    TextView totalPageLabel;

    @InstanceState
    @FragmentArg
    int totalPage;
    @InstanceState
    @FragmentArg
    String executionId;
    @InstanceState
    @FragmentArg
    String exportType;

    @InstanceState
    int currentPage = FIRST_PAGE;

    @Inject
    JsRestClient jsRestClient;

    private WebViewFragment webViewFragment;

    @AfterViews
    final void init() {
        webViewFragment = (WebViewFragment) getFragmentManager()
                .findFragmentByTag(WebViewFragment.TAG);
        currentPageLabel.setText(String.valueOf(currentPage));
        totalPageLabel.setText(getString(R.string.of, totalPage));
        paginateToCurrentSelection();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean showControl = (totalPage > 1);
        view.setVisibility(showControl ? View.VISIBLE : View.GONE);
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
        NumberDialogFragment.show(getFragmentManager(), currentPage, totalPage,
                new NumberDialogFragment.OnPageSelectedListener() {
                    @Override
                    public void onPageSelected(int page) {
                        currentPage = page;
                        paginateToCurrentSelection();
                    }
                });
    }

    private void paginateToCurrentSelection() {
        alterControlStates();
        loadPage();
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

    private void loadPage() {
        if (webViewFragment != null) {
            String exportOutput = String.format("%s;pages=%d", exportType, currentPage);
            URI reportUri = jsRestClient.getExportOuptutResourceURI(executionId, exportOutput);

            webViewFragment.loadUrl(reportUri.toString());
        }
    }

}
