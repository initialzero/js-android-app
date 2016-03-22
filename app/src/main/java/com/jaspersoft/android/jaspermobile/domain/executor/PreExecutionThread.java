package com.jaspersoft.android.jaspermobile.domain.executor;

import rx.Scheduler;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface PreExecutionThread {
    Scheduler getScheduler();
}
