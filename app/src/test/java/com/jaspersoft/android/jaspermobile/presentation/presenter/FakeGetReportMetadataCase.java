package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportMetadataCase;
import com.jaspersoft.android.jaspermobile.visualize.ReportData;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class FakeGetReportMetadataCase extends GetReportMetadataCase {
    private ReportResource mResource;

    protected FakeGetReportMetadataCase() {
        super(FakePreExecutionThread.create(), FakePostExecutionThread.create(), null, null, null);
    }

    public void setResource(ReportResource resource) {
        mResource = resource;
    }

    @Override
    protected Observable<ReportResource> buildUseCaseObservable(ReportData reportData) {
        return Observable.just(mResource);
    }
}
