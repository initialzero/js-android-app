package com.jaspersoft.android.jaspermobile.presentation.presenter;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.ReportControlFlags;
import com.jaspersoft.android.jaspermobile.domain.interactor.report.GetReportShowControlsPropertyCase;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class FakeGetReportShowControlsPropertyCase extends GetReportShowControlsPropertyCase {
    private ReportControlFlags mFakeResult;

    public FakeGetReportShowControlsPropertyCase() {
        super(FakePreExecutionThread.create(), FakePostExecutionThread.create(), null, null);
    }

    public void setNeedParams(boolean hasControls) {
        mFakeResult = new ReportControlFlags(true, hasControls);
    }

    @Override
    protected Observable<ReportControlFlags> buildUseCaseObservable(String reportUri) {
        return Observable.just(mFakeResult);
    }
}