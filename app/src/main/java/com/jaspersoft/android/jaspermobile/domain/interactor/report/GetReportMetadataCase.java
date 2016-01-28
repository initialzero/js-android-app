package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.domain.AppResource;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.visualize.ReportData;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetReportMetadataCase extends AbstractUseCase<AppResource, ReportData> {

    @Inject
    public GetReportMetadataCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread
    ) {
        super(preExecutionThread, postExecutionThread);
    }

    @Override
    protected Observable<AppResource> buildUseCaseObservable(ReportData reportData) {
        return null;
    }
}
