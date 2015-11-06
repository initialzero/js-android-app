package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;

import rx.Subscription;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public abstract class UseCase {

    private final PreExecutionThread mPreExecutionThread;
    private final PostExecutionThread mPostExecutionThread;
    private Subscription mSubscription;

    protected UseCase(PreExecutionThread threadExecutor, PostExecutionThread postExecutionThread) {
        mPreExecutionThread = threadExecutor;
        mPostExecutionThread = postExecutionThread;
    }

    protected abstract rx.Observable buildUseCaseObservable();

    @SuppressWarnings("unchecked")
    public void execute(rx.Subscriber useCaseSubscriber) {
        mSubscription = buildUseCaseObservable()
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
