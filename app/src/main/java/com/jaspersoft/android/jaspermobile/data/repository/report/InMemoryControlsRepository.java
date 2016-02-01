package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.cache.report.ControlsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.InputControlsMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.network.entity.control.InputControl;
import com.jaspersoft.android.sdk.service.rx.filter.RxFiltersService;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryControlsRepository implements ControlsRepository {
    private final RxFiltersService mFiltersService;
    private final ControlsCache mControlsCache;
    private final ReportParamsCache mReportParamsCache;
    private final InputControlsMapper mControlsMapper;
    private final ReportParamsMapper mReportParamsMapper;

    private Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> mListInputControlsAction;
    private Observable<List<InputControlState>> mValidateControlsValuesAction;
    private Observable<List<InputControlState>> mListControlsValuesAction;

    @Inject
    public InMemoryControlsRepository(RxFiltersService filtersService,
                                      ControlsCache controlsCache,
                                      ReportParamsCache reportParamsCache, InputControlsMapper controlsMapper,
                                      ReportParamsMapper reportParamsMapper) {
        mFiltersService = filtersService;
        mControlsCache = controlsCache;
        mReportParamsCache = reportParamsCache;
        mControlsMapper = controlsMapper;
        mReportParamsMapper = reportParamsMapper;
    }

    @NonNull
    @Override
    public Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> listReportControls(@NonNull final String reportUri) {
        return createListControls(reportUri, mFiltersService.listReportControls(reportUri));
    }

    @NonNull
    @Override
    public Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> listDashboardControls(@NonNull String dashboardUri) {
        return createListControls(dashboardUri, mFiltersService.listDashboardControls(dashboardUri));
    }

    @NonNull
    private Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> createListControls(final String reportUri, final Observable<List<InputControl>> networkCall) {
        if (mListInputControlsAction == null) {
            Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> memorySource = Observable.defer(
                    new Func0<Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>>>() {
                        @Override
                        public Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> call() {
                            List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> inputControls = mControlsCache.get(reportUri);
                            if (inputControls == null) {
                                return Observable.empty();
                            }
                            return Observable.just(inputControls);
                        }
                    });
            Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> networkSource = Observable.defer(
                    new Func0<Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>>>() {
                        @Override
                        public Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> call() {
                            return networkCall
                                    .map(new Func1<List<InputControl>, List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>>() {
                                        @Override
                                        public List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> call(List<InputControl> inputControls) {
                                            return mControlsMapper.retrofittedControlsToLegacy(inputControls);
                                        }
                                    })
                                    .doOnNext(new Action1<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>>() {
                                        @Override
                                        public void call(List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> inputControls) {
                                            mControlsCache.put(reportUri, inputControls);
                                            List<ReportParameter> parameters = mReportParamsMapper.legacyControlsToParams(inputControls);
                                            mReportParamsCache.put(reportUri, parameters);
                                        }
                                    });
                        }
                    });
            mListInputControlsAction = Observable.concat(memorySource, networkSource)
                    .first()
                    .cache()
                    .doOnTerminate(new Action0() {
                        @Override
                        public void call() {
                            mListInputControlsAction = null;
                        }
                    });
        }

        return mListInputControlsAction;
    }

    @NonNull
    @Override
    public Observable<List<InputControlState>> validateControls(@NonNull final String reportUri) {
        if (mValidateControlsValuesAction == null) {
            mValidateControlsValuesAction = Observable.defer(
                    new Func0<Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>>>() {
                        @Override
                        public Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>> call() {
                            List<ReportParameter> parameters = mReportParamsCache.get(reportUri);
                            if (parameters == null) {
                                return Observable.just(Collections.<com.jaspersoft.android.sdk.network.entity.control.InputControlState>emptyList());
                            }
                            List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> params =
                                    mReportParamsMapper.legacyParamsToRetrofitted(parameters);
                            return mFiltersService.validateControls(reportUri, params, true);
                        }
                    }).map(new Func1<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>, List<InputControlState>>() {
                @Override
                public List<InputControlState> call(List<com.jaspersoft.android.sdk.network.entity.control.InputControlState> inputControlStates) {
                    return mControlsMapper.retrofittedStatesToLegacy(inputControlStates);
                }
            }).doOnTerminate(new Action0() {
                @Override
                public void call() {
                    mValidateControlsValuesAction = null;
                }
            }).cache();
        }

        return mValidateControlsValuesAction;
    }

    @NonNull
    @Override
    public Observable<List<InputControlState>> listControlValues(@NonNull final String reportUri) {
        if (mListControlsValuesAction == null) {
            mListControlsValuesAction = Observable.defer(new Func0<Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>>>() {
                @Override
                public Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>> call() {
                    List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> inputControls = mControlsCache.get(reportUri);
                    List<ReportParameter> parameters = mReportParamsMapper.legacyControlsToParams(inputControls);
                    if (parameters == null) {
                        return Observable.just(Collections.<com.jaspersoft.android.sdk.network.entity.control.InputControlState>emptyList());
                    }
                    List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> params =
                            mReportParamsMapper.legacyParamsToRetrofitted(parameters);
                    return mFiltersService.listControlsStates(reportUri, params, true);
                }
            }).map(new Func1<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>, List<InputControlState>>() {
                @Override
                public List<InputControlState> call(List<com.jaspersoft.android.sdk.network.entity.control.InputControlState> inputControlStates) {
                    return mControlsMapper.retrofittedStatesToLegacy(inputControlStates);
                }
            }).doOnTerminate(new Action0() {
                @Override
                public void call() {
                    mListControlsValuesAction = null;
                }
            }).cache();
        }
        return mListControlsValuesAction;
    }

    @Override
    public void flushControls(@NonNull String reportUri) {
        mControlsCache.evict(reportUri);
    }
}
