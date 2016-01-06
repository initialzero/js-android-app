package com.jaspersoft.android.jaspermobile.domain.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.Report;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportPageRepository {
    @NonNull
    Observable<ReportPage> get(@NonNull Report report, @NonNull String pagePosition);
}
