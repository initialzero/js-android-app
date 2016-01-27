package com.jaspersoft.android.jaspermobile.presentation.presenter;

import android.support.annotation.VisibleForTesting;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.SimpleSubscriber;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.FlushReportCachesCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportMultiPagePropertyCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportPageContentCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportTotalPagesPropertyCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.ReloadReportCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.RunReportCase;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.UpdateReportCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.action.ReportActionListener;
import com.jaspersoft.android.jaspermobile.presentation.view.ReportView;
import com.jaspersoft.android.sdk.service.exception.ServiceException;
import com.jaspersoft.android.sdk.service.exception.StatusCodes;

import javax.inject.Inject;
import javax.inject.Named;

import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class ReportViewPresenter implements ReportActionListener, Presenter<ReportView> {
    private ReportView mView;

    private final String mReportUri;
    private final RequestExceptionHandler mExceptionHandler;
    private final GetReportShowControlsPropertyCase mGetReportShowControlsPropertyCase;
    private final GetReportMultiPagePropertyCase mGetReportMultiPagePropertyCase;
    private final GetReportTotalPagesPropertyCase mGetReportTotalPagesPropertyCase;
    private final GetReportPageContentCase mGetReportPageContentCase;
    private final RunReportCase mRunReportCase;
    private final UpdateReportCase mUpdateReportCase;
    private final ReloadReportCase mReloadReportCase;
    private final FlushReportCachesCase mFlushReportCachesCase;

    @Inject
    public ReportViewPresenter(
            @Named("report_uri") String reportUri,
            RequestExceptionHandler exceptionHandler,
            GetReportShowControlsPropertyCase getReportShowControlsPropertyCase,
            GetReportMultiPagePropertyCase getReportMultiPagePropertyCase,
            GetReportTotalPagesPropertyCase getReportTotalPagesPropertyCase,
            GetReportPageContentCase getReportPageContentCase,
            RunReportCase runReportCase,
            UpdateReportCase updateReportCase,
            ReloadReportCase reloadReportCase,
            FlushReportCachesCase flushReportCachesCase) {
        mReportUri = reportUri;
        mExceptionHandler = exceptionHandler;
        mGetReportShowControlsPropertyCase = getReportShowControlsPropertyCase;
        mGetReportMultiPagePropertyCase = getReportMultiPagePropertyCase;
        mGetReportTotalPagesPropertyCase = getReportTotalPagesPropertyCase;
        mGetReportPageContentCase = getReportPageContentCase;
        mRunReportCase = runReportCase;
        mUpdateReportCase = updateReportCase;
        mReloadReportCase = reloadReportCase;
        mFlushReportCachesCase = flushReportCachesCase;
    }

    public void init() {
        if (mView == null) {
            throw new NullPointerException("Please inject view before calling this method");
        }
        if (mView.getState().isControlsPageShown()) {
            loadLastSavedPage();
            loadMultiPageProperty();
            loadTotalPagesProperty();
        } else {
            loadReportMetadata();
        }
    }

    private void loadLastSavedPage() {
        loadPageByPosition(mView.getState().getCurrentPage());
    }

    private void loadPageByPosition(final String position) {
        showPageLoader();
        PageRequest request = new PageRequest(mReportUri, position);
        mGetReportPageContentCase.execute(request, new SimpleSubscriber<ReportPage>() {
            @Override
            public void onError(Throwable e) {
                handleError(e);
            }

            @Override
            public void onNext(ReportPage item) {
                showPage(position, item);
            }
        });
    }

    private void loadReportMetadata() {
        showLoading();
        mGetReportShowControlsPropertyCase.execute(mReportUri, new SimpleSubscriber<Boolean>() {
            @Override
            public void onCompleted() {
                hideLoading();
            }

            @Override
            public void onError(Throwable e) {
                handleError(e);
            }

            @Override
            public void onNext(Boolean needControls) {
                mView.getState().setControlsPageShown(true);
                toggleFiltersAction(needControls);
                resolveNeedControls(needControls);
            }
        });
    }

    private void resolveNeedControls(boolean needControls) {
        if (needControls) {
            mView.showInitialFiltersPage();
        } else {
            runReport();
        }
    }

    @Override
    public void injectView(ReportView view) {
        mView = view;
    }

    @Override
    public void resume() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void destroy() {
        mGetReportShowControlsPropertyCase.unsubscribe();
        mGetReportMultiPagePropertyCase.unsubscribe();
        mGetReportTotalPagesPropertyCase.unsubscribe();
        mGetReportPageContentCase.unsubscribe();
        mRunReportCase.unsubscribe();
        mUpdateReportCase.unsubscribe();
        mReloadReportCase.unsubscribe();
        mFlushReportCachesCase.execute(mReportUri);
    }

    @Override
    public void loadPage(String pageRange) {
        loadPageByPosition(pageRange);
    }

    @Override
    public void runReport() {
        showLoading();
        mRunReportCase.execute(mReportUri, new SimpleSubscriber<ReportPage>() {
            @Override
            public void onCompleted() {
                hideLoading();
            }

            @Override
            public void onError(Throwable e) {
                handleError(e);
            }

            @Override
            public void onNext(ReportPage page) {
                showPage("1", page);
                loadMultiPageProperty();
                loadTotalPagesProperty();
            }
        });
    }

    @VisibleForTesting
    void loadMultiPageProperty() {
        mGetReportMultiPagePropertyCase.execute(mReportUri, new SimpleSubscriber<Boolean>() {
            @Override
            public void onError(Throwable e) {
                handleError(e);
            }

            @Override
            public void onNext(Boolean multiPage) {
                togglePaginationControl(multiPage);
            }
        });
    }

    @VisibleForTesting
    void loadTotalPagesProperty() {
        mGetReportTotalPagesPropertyCase.execute(mReportUri, new SimpleSubscriber<Integer>() {
            @Override
            public void onError(Throwable e) {
                handleError(e);
            }

            @Override
            public void onNext(Integer totalPages) {
                boolean hasNoPages = (totalPages == 0);
                toggleSaveAction(!hasNoPages);

                if (hasNoPages) {
                    showEmptyPage();
                } else {
                    updateTotalPagesLabel(totalPages);
                }
            }
        });
    }

    @Override
    public void updateReport() {
        showLoading();
        resetTotalPagesLabel();
        mUpdateReportCase.execute(mReportUri, new SimpleSubscriber<ReportPage>() {
            @Override
            public void onCompleted() {
                hideLoading();
            }

            @Override
            public void onError(Throwable e) {
                handleError(e);
            }

            @Override
            public void onNext(ReportPage page) {
                showPage("1", page);
                loadMultiPageProperty();
                loadTotalPagesProperty();
            }
        });
    }

    @Override
    public void refresh() {
        reloadByPosition("1");
    }

    private void reloadByPosition(final String position) {
        showLoading();
        resetTotalPagesLabel();
        mReloadReportCase.execute(mReportUri, new SimpleSubscriber<ReportPage>() {
            @Override
            public void onCompleted() {
                hideLoading();
            }

            @Override
            public void onError(Throwable e) {
                handleError(e);
            }

            @Override
            public void onNext(ReportPage page) {
                showPage(position, page);
                loadMultiPageProperty();
                loadTotalPagesProperty();
            }
        });
    }

    private void showPageLoader() {
        mView.showPageLoader();
    }

    private void showPageOutOfRangeError() {
        mView.showPageOutOfRangeError();
    }

    private void showEmptyPage() {
        mView.showEmptyPageMessage();
    }

    private void resetTotalPagesLabel() {
        mView.resetPaginationControl();
    }

    private void updateTotalPagesLabel(int pages) {
        mView.showTotalPages(pages);
    }

    private void toggleSaveAction(boolean visibility) {
        mView.setSaveActionVisibility(visibility);
        mView.reloadMenu();
    }

    private void toggleFiltersAction(boolean visibility) {
        mView.setFilterActionVisibility(visibility);
        mView.reloadMenu();
    }

    private void togglePaginationControl(boolean multiPage) {
        mView.setPaginationControlVisibility(multiPage);
    }

    private void hideLoading() {
        mView.hideLoading();
    }

    private void showLoading() {
        mView.showLoading();
    }

    @VisibleForTesting
    void showPage(String pagePosition, ReportPage page) {
        mView.hideError();
        mView.showCurrentPage(Integer.valueOf(pagePosition));
        mView.getState().setCurrentPage(pagePosition);
        mView.showPage(page.getContent());
    }

    @VisibleForTesting
    void handleError(Throwable error) {
        if (error instanceof ServiceException) {
            ServiceException serviceException = (ServiceException) error;
            tryToRecoverFromSdkError(serviceException);
        } else {
            Timber.e(error, "Presenter received unexpected error");
            showErrorMessage(error);
        }
    }

    private void tryToRecoverFromSdkError(ServiceException serviceException) {
        int errorCode = serviceException.code();
        switch (errorCode) {
            case StatusCodes.EXPORT_PAGE_OUT_OF_RANGE:
                showPageOutOfRangeError();
                loadLastSavedPage();
                break;
            case StatusCodes.REPORT_EXECUTION_INVALID:
                reloadByPosition(mView.getState().getCurrentPage());
                break;
            default:
                Timber.e(serviceException, "Page request operation crashed with SDK exception");
                showErrorMessage(serviceException);
        }
    }

    private void showErrorMessage(Throwable error) {
        mView.hideLoading();
        mView.showError(mExceptionHandler.extractMessage(error));
    }
}
