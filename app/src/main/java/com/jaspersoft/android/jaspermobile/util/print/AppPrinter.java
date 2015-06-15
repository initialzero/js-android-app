package com.jaspersoft.android.jaspermobile.util.print;

import org.roboguice.shaded.goole.common.annotations.VisibleForTesting;

import java.io.File;

import rx.Observable;
import rx.Subscription;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class AppPrinter implements ResourcePrinter {
    private final ObservableResourceProvider mResourceProvider;
    private final ResourcePrintJob resourcePrintJob;

    private Subscription mSubscription;
    private Observable<File> mResourceTask;

    private AppPrinter(Builder builder) {
        mResourceProvider = builder.resourceProvider;
        resourcePrintJob = builder.resourcePrintJob;
    }

    public static Builder builder() {
        return new Builder();
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
        private ObservableResourceProvider resourceProvider;
        private ResourcePrintJob resourcePrintJob;

        @VisibleForTesting
        Builder setResourceProvider(ObservableResourceProvider resourceProvider) {
            this.resourceProvider = resourceProvider;
            return this;
        }

        public Builder setResourceProvider(FileResourceProvider resourceProvider) {
            this.resourceProvider = ResourceProviderDecorator.decorate(resourceProvider);
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
