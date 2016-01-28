package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.AppResource;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportMetadataCase;
import com.jaspersoft.android.jaspermobile.visualize.ReportData;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class FakeGetReportMetadataCase extends GetReportMetadataCase {
    private AppResource mResource;

    protected FakeGetReportMetadataCase() {
        super(FakePreExecutionThread.create(), FakePostExecutionThread.create(), null, null, null);
    }

    public void setResource(AppResource resource) {
        mResource = resource;
    }

    @Override
    protected Observable<AppResource> buildUseCaseObservable(ReportData reportData) {
        return Observable.just(mResource);
    }
}
