package com.jaspersoft.android.jaspermobile.domain.interactor;

import com.jaspersoft.android.jaspermobile.util.rx.RxTransformer;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.Subscriptions;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public abstract class AbstractUseCase<Result, Argument> implements UseCase<Result, Argument> {
    private Subscription subscription = Subscriptions.empty();

    /**
     * Builds an {@link rx.Observable} which will be used when executing the current {@link AbstractSimpleUseCase}.
     */
    protected abstract Observable<Result> buildUseCaseObservable(Argument argument);

    /**
     * Executes the current use case.
     *
     * @param useCaseSubscriber The guy who will be listen to the observable build with {@link #buildUseCaseObservable(Argument)}.
     */
    @Override
    public final void execute(Argument argument, Subscriber<? super Result> useCaseSubscriber) {
        this.subscription = this.buildUseCaseObservable(argument)
                .compose(RxTransformer.<Result>applySchedulers())
                .subscribe(useCaseSubscriber);
    }

    /**
     * Unsubscribes from current {@link rx.Subscription}.
     */
    @Override
    public final void unsubscribe() {
        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }
}
