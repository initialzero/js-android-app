package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Subscription;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class CompositeUseCase {
    private final PreExecutionThread mPreExecutionThread;
    private final PostExecutionThread mPostExecutionThread;
    private Subscription mSubscription;

    @Inject
    public CompositeUseCase(PostExecutionThread postExecutionThread, PreExecutionThread threadExecutor) {
        mPostExecutionThread = postExecutionThread;
        mPreExecutionThread = threadExecutor;
    }

    @SuppressWarnings("unchecked")
    public void execute(rx.Observable observable, rx.Subscriber useCaseSubscriber) {
        mSubscription = observable
                .subscribeOn(mPreExecutionThread.getScheduler())
                .observeOn(mPostExecutionThread.getScheduler())
                .subscribe(useCaseSubscriber);
    }

    public void unsubscribe() {
        if (!mSubscription.isUnsubscribed()) {
            mSubscription.unsubscribe();
        }
    }

}
