package com.jaspersoft.android.jaspermobile;

import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class UIThread implements PostExecutionThread {

    @Inject
    public UIThread() {}

    @Override
    public Scheduler getScheduler() {
        return AndroidSchedulers.mainThread();
    }
}
