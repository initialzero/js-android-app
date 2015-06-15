package com.jaspersoft.android.jaspermobile.util.print;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class AppPrinter implements ResourcePrinter {
    private final ResourcePrintJob resourcePrintJob;
    private final ResourcePrintJob.Listener resourcePrintListener;

    private Subscription mSubscription;
    private Observable mPrintTask;

    private AppPrinter(Builder builder) {
        this.resourcePrintJob = builder.resourcePrintJob;
        this.resourcePrintListener = builder.resourcePrintListener;
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void print() {
        mPrintTask = resourcePrintJob.printResource().cache();
        mSubscription = subscribeOnPrinting(mPrintTask);
    }

    @Override
    public void resume() {
        if (mPrintTask != null) {
            mSubscription = subscribeOnPrinting(mPrintTask);
        }
    }

    @Override
    public void pause() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    private Subscription subscribeOnPrinting(Observable task) {
        if (task == null) {
            throw new IllegalStateException("Printing task is null");
        }
        return task.subscribe(new Action1() {
            @Override
            public void call(Object o) {
                resourcePrintListener.onSuccess();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                resourcePrintListener.onError(throwable);
            }
        });
    }

    public static class Builder {
        private ResourcePrintJob resourcePrintJob;
        private ResourcePrintJob.Listener resourcePrintListener;

        public Builder setResourcePrintJob(ResourcePrintJob resourcePrintJob) {
            this.resourcePrintJob = resourcePrintJob;
            return this;
        }

        public Builder setResourcePrintListener(ResourcePrintJob.Listener resourcePrintListener) {
            this.resourcePrintListener = resourcePrintListener;
            return this;
        }

        public ResourcePrinter build() {
            validateDependencies();
            ensureSaneDefaults();
            return new AppPrinter(this);
        }

        private void validateDependencies() {
            if (resourcePrintJob == null) {
                throw new IllegalStateException("Resource print job should not be null");
            }
        }

        private void ensureSaneDefaults() {
            if (resourcePrintListener == null) {
                resourcePrintListener = new NullPrintListener();
            }
        }
    }

    private static class NullPrintListener implements ResourcePrintJob.Listener {
        @Override
        public void onSuccess() {
        }

        @Override
        public void onError(Throwable throwable) {
        }
    }
}
