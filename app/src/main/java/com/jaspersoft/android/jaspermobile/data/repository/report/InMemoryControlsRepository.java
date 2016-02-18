package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.cache.report.ControlsCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.ReportParamsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.InputControlsMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ReportParamsMapper;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.network.entity.control.InputControl;
import com.jaspersoft.android.sdk.service.data.dashboard.DashboardControlComponent;
import com.jaspersoft.android.sdk.service.rx.filter.RxFiltersService;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryControlsRepository implements ControlsRepository {
    private final ControlsCache mControlsCache;
    private final ReportParamsCache mReportParamsCache;
    private final InputControlsMapper mControlsMapper;
    private final ReportParamsMapper mReportParamsMapper;
    private final JasperRestClient mRestClient;

    @Inject
    public InMemoryControlsRepository(JasperRestClient restClient,
                                      ControlsCache controlsCache,
                                      ReportParamsCache reportParamsCache, InputControlsMapper controlsMapper,
                                      ReportParamsMapper reportParamsMapper) {
        mRestClient = restClient;
        mControlsCache = controlsCache;
        mReportParamsCache = reportParamsCache;
        mControlsMapper = controlsMapper;
        mReportParamsMapper = reportParamsMapper;
    }

    @NonNull
    @Override
    public Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> listReportControls(@NonNull final String reportUri) {
        Observable<List<InputControl>> listObservable = mRestClient.filtersService().flatMap(new Func1<RxFiltersService, Observable<List<InputControl>>>() {
            @Override
            public Observable<List<InputControl>> call(RxFiltersService service) {
                return service.listReportControls(reportUri);
            }
        });

        return createListControls(reportUri, listObservable);
    }

    @NonNull
    @Override
    public Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> listDashboardControls(@NonNull final String dashboardUri) {
        Observable<List<InputControl>> listObservable = mRestClient.filtersService().flatMap(new Func1<RxFiltersService, Observable<List<InputControl>>>() {
            @Override
            public Observable<List<InputControl>> call(RxFiltersService service) {
                return service.listDashboardControls(dashboardUri);
            }
        });
        return createListControls(dashboardUri, listObservable);
    }

    @NonNull
    private Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> createListControls(final String reportUri, final Observable<List<InputControl>> networkCall) {
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
                                        boolean containsParams = mReportParamsCache.contains(reportUri);
                                        if (!containsParams) {
                                            List<ReportParameter> parameters = mReportParamsMapper.legacyControlsToParams(inputControls);
                                            mReportParamsCache.put(reportUri, parameters);
                                        }
                                    }
                                });
                    }
                });
        return Observable.concat(memorySource, networkSource).first();
    }

    @NonNull
    @Override
    public Observable<List<InputControlState>> validateControls(@NonNull final String reportUri) {
        return Observable.defer(
                new Func0<Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>>>() {
                    @Override
                    public Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>> call() {
                        List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> controls =
                                mControlsCache.get(reportUri);
                        List<ReportParameter> parameters = mReportParamsMapper.legacyControlsToParams(controls);
                        if (parameters == null) {
                            return Observable.just(Collections.<com.jaspersoft.android.sdk.network.entity.control.InputControlState>emptyList());
                        }
                        final List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> params =
                                mReportParamsMapper.legacyParamsToRetrofitted(parameters);
                        return mRestClient.filtersService()
                                .flatMap(new Func1<RxFiltersService, Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>>>() {
                                    @Override
                                    public Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>> call(RxFiltersService service) {
                                        return service.validateControls(reportUri, params, true);
                                    }
                                });
                    }
                }).map(new Func1<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>, List<InputControlState>>() {
            @Override
            public List<InputControlState> call(List<com.jaspersoft.android.sdk.network.entity.control.InputControlState> inputControlStates) {
                return mControlsMapper.retrofittedStatesToLegacy(inputControlStates);
            }
        });
    }

    @NonNull
    @Override
    public Observable<List<InputControlState>> listControlValues(@NonNull final String reportUri) {
        return Observable.defer(new Func0<Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>>>() {
            @Override
            public Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>> call() {
                List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> inputControls = mControlsCache.get(reportUri);
                List<ReportParameter> parameters = mReportParamsMapper.legacyControlsToParams(inputControls);
                if (parameters == null) {
                    return Observable.just(Collections.<com.jaspersoft.android.sdk.network.entity.control.InputControlState>emptyList());
                }
                final List<com.jaspersoft.android.sdk.network.entity.report.ReportParameter> params =
                        mReportParamsMapper.legacyParamsToRetrofitted(parameters);
                return mRestClient.filtersService()
                        .flatMap(new Func1<RxFiltersService, Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>>>() {
                            @Override
                            public Observable<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>> call(RxFiltersService service) {
                                return service.listControlsStates(reportUri, params, true);
                            }
                        });
            }
        }).map(new Func1<List<com.jaspersoft.android.sdk.network.entity.control.InputControlState>, List<InputControlState>>() {
            @Override
            public List<InputControlState> call(List<com.jaspersoft.android.sdk.network.entity.control.InputControlState> inputControlStates) {
                return mControlsMapper.retrofittedStatesToLegacy(inputControlStates);
            }
        });

    }

    @NonNull
    @Override
    public Observable<List<DashboardControlComponent>> listDashboardControlComponents(@NonNull final String dashboardUri) {
        return mRestClient.filtersService()
                .flatMap(new Func1<RxFiltersService, Observable<List<DashboardControlComponent>>>() {
                    @Override
                    public Observable<List<DashboardControlComponent>> call(RxFiltersService service) {
                        return service.listDashboardControlComponents(dashboardUri);
                    }
                });
    }

    @Override
    public void flushControls(@NonNull String reportUri) {
        mControlsCache.evict(reportUri);
    }
}
