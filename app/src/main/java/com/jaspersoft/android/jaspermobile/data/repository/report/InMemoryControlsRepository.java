package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.data.cache.report.ControlsCache;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.InputControlsMapper;
import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.network.entity.control.InputControl;
import com.jaspersoft.android.sdk.service.rx.report.RxFiltersService;

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
    private final InputControlsMapper mControlsMapper;

    private Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> mGetInputControlsCommand;

    @Inject
    public InMemoryControlsRepository(RxFiltersService filtersService, ControlsCache controlsCache, InputControlsMapper controlsMapper) {
        mFiltersService = filtersService;
        mControlsCache = controlsCache;
        mControlsMapper = controlsMapper;
    }

    @NonNull
    @Override
    public Observable<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>> listControls(@NonNull final String reportUri) {
        if (mGetInputControlsCommand == null) {
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
                            return mFiltersService.listControls(reportUri)
                                    .map(new Func1<List<InputControl>, List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>>() {
                                        @Override
                                        public List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> call(List<InputControl> inputControls) {
                                            return mControlsMapper.transform(inputControls);
                                        }
                                    })
                                    .doOnNext(new Action1<List<com.jaspersoft.android.sdk.client.oxm.control.InputControl>>() {
                                        @Override
                                        public void call(List<com.jaspersoft.android.sdk.client.oxm.control.InputControl> inputControls) {
                                            mControlsCache.put(reportUri, inputControls);
                                        }
                                    });
                        }
                    });
            mGetInputControlsCommand = Observable.concat(memorySource, networkSource)
                    .first()
                    .cache()
                    .doOnCompleted(new Action0() {
                        @Override
                        public void call() {
                            mGetInputControlsCommand = null;
                        }
                    });
        }

        return mGetInputControlsCommand;
    }
}
