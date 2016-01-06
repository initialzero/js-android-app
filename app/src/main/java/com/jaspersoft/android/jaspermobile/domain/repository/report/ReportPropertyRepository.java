package com.jaspersoft.android.jaspermobile.domain.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.Report;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportPropertyRepository {
    @NonNull
    Observable<Boolean> getMultiPageProperty(@NonNull Report report);
    @NonNull
    Observable<Integer> getTotalPagesProperty(@NonNull Report report);
}
