package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class FakeGetReportShowControlsPropertyCase extends GetReportShowControlsPropertyCase {
    private boolean mFakeResult;

    public FakeGetReportShowControlsPropertyCase() {
        super(FakePreExecutionThread.create(), FakePostExecutionThread.create(), null, resourceRepository, reportParamsCache);
    }

    public void setNeedParams(boolean fakeResult) {
        mFakeResult = fakeResult;
    }

    @Override
    protected Observable<Boolean> buildUseCaseObservable(String reportUri) {
        return Observable.just(mFakeResult);
    }
}