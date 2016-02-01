package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ResourceRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;
import com.jaspersoft.android.sdk.service.rx.repository.RxRepositoryService;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryResourceRepository implements ResourceRepository {
    private final JasperRestClient mRestClient;
    private Observable<ReportResource> mGetReportDetailsAction;

    @Inject
    public InMemoryResourceRepository(JasperRestClient restClient) {
        mRestClient = restClient;
    }

    @Override
    public Observable<ReportResource> getReportResource(@NonNull final String reportUri) {
        if (mGetReportDetailsAction == null) {

            mGetReportDetailsAction = mRestClient.repositoryService()
                    .flatMap(new Func1<RxRepositoryService, Observable<ReportResource>>() {
                        @Override
                        public Observable<ReportResource> call(RxRepositoryService service) {
                            return service.fetchReportDetails(reportUri);
                        }
                    })
                    .doOnTerminate(new Action0() {
                        @Override
                        public void call() {
                            mGetReportDetailsAction = null;
                        }
                    }).cache();
        }

        return mGetReportDetailsAction;
    }
}
