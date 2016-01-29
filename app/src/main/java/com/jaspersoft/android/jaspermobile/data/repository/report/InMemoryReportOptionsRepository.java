package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.InputControlsMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ReportOptionsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.service.data.report.option.ReportOption;
import com.jaspersoft.android.sdk.service.rx.filter.RxFiltersService;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryReportOptionsRepository implements ReportOptionsRepository {

    private final RxFiltersService mFiltersService;
    private final ReportParamsMapper mReportParamsMapper;
    private final InputControlsMapper mControlsMapper;

    private Observable<Set<ReportOption>> mGetReportOptionsAction;
    private Observable<ReportOption> mCreateReportOptionAction;
    private Observable<Void> mDeleteReportOptionAction;
    private Observable<List<InputControlState>> mGetReportOptionStatesAction;

    @Inject
    public InMemoryReportOptionsRepository(
            RxFiltersService filtersService,
            ReportParamsMapper reportParamsMapper,
            InputControlsMapper controlsMapper
    ) {
        mFiltersService = filtersService;
        mReportParamsMapper = reportParamsMapper;
        mControlsMapper = controlsMapper;
    }

    @NonNull
    @Override
    public Observable<Set<ReportOption>> getReportOption(@NonNull final String reportUri) {
        if (mGetReportOptionsAction == null) {
            mGetReportOptionsAction = Observable.defer(new Func0<Observable<Set<ReportOption>>>() {
                @Override
                public Observable<Set<ReportOption>> call() {
                    return mFiltersService.listReportOptions(reportUri);
                }
            }).doOnTerminate(new Action0() {
                @Override
                public void call() {
                    mGetReportOptionsAction = null;
                }
            }).cache();
        }
        return mGetReportOptionsAction;
    }

    @NonNull
    @Override
    public Observable<ReportOption> createReportOptionWithOverride(@NonNull final String reportUri,
                                                                   @NonNull final String label,
                                                                   @NonNull final List<ReportParameter> params) {
        if (mCreateReportOptionAction == null) {
            mCreateReportOptionAction = Observable.defer(new Func0<Observable<ReportOption>>() {
                @Override
                public Observable<ReportOption> call() {
                    List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> reportParameters =
                            mReportParamsMapper.legacyParamsToRetrofitted(params);
                    return mFiltersService.createReportOption(reportUri, label, reportParameters, true);
                }
            }).doOnTerminate(new Action0() {
                @Override
                public void call() {
                    mCreateReportOptionAction = null;
                }
            }).cache();
        }
        return mCreateReportOptionAction;
    }

    @NonNull
    @Override
    public Observable<Void> deleteReportOption(@NonNull final String uri, @NonNull final String optionId) {
        if (mDeleteReportOptionAction == null) {
            mDeleteReportOptionAction = Observable.defer(new Func0<Observable<Void>>() {
                @Override
                public Observable<Void> call() {
                    return mFiltersService.deleteReportOption(uri, optionId);
                }
            }).doOnTerminate(new Action0() {
                @Override
                public void call() {
                    mDeleteReportOptionAction = null;
                }
            }).cache();
        }
        return mDeleteReportOptionAction;
    }

    @NonNull
    @Override
    public Observable<List<InputControlState>> getReportOptionStates(@NonNull final String reportUri) {
        if (mGetReportOptionStatesAction == null) {
            mGetReportOptionStatesAction = Observable.defer(new Func0<Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>>>() {
                @Override
                public Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>> call() {
                    return mFiltersService.listResourceStates(reportUri, true);
                }
            }).map(new Func1<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>, List<InputControlState>>() {
                @Override
                public List<InputControlState> call(List<com.jaspersoft.android.sdk.network.entity.control.InputControlState> inputControlStates) {
                    return mControlsMapper.retrofittedStatesToLegacy(inputControlStates);
                }
            }).doOnTerminate(new Action0() {
                @Override
                public void call() {
                    mGetReportOptionStatesAction = null;
                }
            }).cache();
        }
        return mGetReportOptionStatesAction;
    }
}
