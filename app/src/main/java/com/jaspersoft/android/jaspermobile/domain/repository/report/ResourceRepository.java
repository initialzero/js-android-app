package com.jaspersoft.android.jaspermobile.domain.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.AppResource;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ResourceRepository {
    Observable<AppResource> getReportResource(@NonNull String reportUri);
}
