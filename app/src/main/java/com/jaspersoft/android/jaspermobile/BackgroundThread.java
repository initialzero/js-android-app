package com.jaspersoft.android.jaspermobile;

import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class BackgroundThread implements PreExecutionThread {
    @Inject
    public BackgroundThread() {
    }

    @Override
    public Scheduler getScheduler() {
        return Schedulers.io();
    }
}