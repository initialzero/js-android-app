package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import android.os.ParcelFileDescriptor;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.PrintRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportPageRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetPrintReportPageCase extends AbstractUseCase<ParcelFileDescriptor, PrintRequest> {
    private final ReportRepository mReportRepository;
    private final ReportPageRepository mReportPageRepository;

    @Inject
    public GetPrintReportPageCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ReportRepository reportRepository,
            ReportPageRepository reportPageRepository
    ) {
        super(preExecutionThread, postExecutionThread);
        mReportRepository = reportRepository;
        mReportPageRepository = reportPageRepository;
    }

    @Override
    protected Observable<ParcelFileDescriptor> buildUseCaseObservable(final PrintRequest request) {
        final PageRequest pageRequest = request.getPageRequest();
        return mReportRepository.getReport(pageRequest.getUri())
                .flatMap(new Func1<RxReportExecution, Observable<ReportPage>>() {
                    @Override
                    public Observable<ReportPage> call(RxReportExecution execution) {
                        return mReportPageRepository.get(execution, pageRequest);
                    }
                }).flatMap(new Func1<ReportPage, Observable<ParcelFileDescriptor>>() {
                    @Override
                    public Observable<ParcelFileDescriptor> call(ReportPage page) {
                        ParcelFileDescriptor destination = request.getDestination();
                        OutputStream output = new FileOutputStream(destination.getFileDescriptor());
                        InputStream stream = new ByteArrayInputStream(page.getContent());
                        try {
                            IOUtils.copy(stream, output);
                        } catch (IOException e) {
                            return Observable.error(e);
                        }
                        IOUtils.closeQuietly(stream);

                        return Observable.just(destination);
                    }
                });
    }
}
