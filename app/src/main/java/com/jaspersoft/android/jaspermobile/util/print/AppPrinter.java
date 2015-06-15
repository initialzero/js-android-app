package com.jaspersoft.android.jaspermobile.util.print;

import android.content.Context;

import java.io.File;

import rx.Observable;
import rx.Subscription;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public class AppPrinter implements ResourcePrinter {
    private final Context mContext;
    private final ResourceProvider mResourceProvider;
    private final ResourcePrintJob resourcePrintJob;

    private Subscription mSubscription;
    private Observable<File> mResourceTask;

    private AppPrinter(Builder builder) {
        mContext = builder.context;
        mResourceProvider = builder.resourceProvider;
        resourcePrintJob = builder.resourcePrintJob;
    }

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    @Override
    public void print() {
        mResourceTask = mResourceProvider.provideResource().cache();
        mSubscription = mResourceTask.subscribe(resourcePrintJob.printResource(), resourcePrintJob.reportError());
    }

    @Override
    public void resume() {
        if (mResourceTask != null) {
            mSubscription = mResourceTask.subscribe(resourcePrintJob.printResource(), resourcePrintJob.reportError());
        }
    }

    @Override
    public void pause() {
        if (mSubscription != null) {
            mSubscription.unsubscribe();
        }
    }

    public static class Builder {
        private final Context context;
        private ResourceProvider resourceProvider;
        private ResourcePrintJob resourcePrintJob;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setResourceProvider(ResourceProvider resourceProvider) {
            this.resourceProvider = resourceProvider;
            return this;
        }

        public Builder setResourcePrintJob(ResourcePrintJob resourcePrintJob) {
            this.resourcePrintJob = resourcePrintJob;
            return this;
        }

        public ResourcePrinter build() {
            validateDependencies();
            return new AppPrinter(this);
        }

        private void validateDependencies() {
            if (resourceProvider == null) {
                throw new IllegalStateException("Resource provider should not be null");
            }
            if (resourcePrintJob == null) {
                throw new IllegalStateException("Resource print job should not be null");
            }
        }
    }
}
