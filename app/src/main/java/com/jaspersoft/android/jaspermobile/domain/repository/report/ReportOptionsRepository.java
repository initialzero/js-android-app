package com.jaspersoft.android.jaspermobile.domain.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;
import com.jaspersoft.android.sdk.service.data.report.option.ReportOption;

import java.util.List;
import java.util.Set;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportOptionsRepository {
    @NonNull
    Observable<Set<ReportOption>> getReportOption(@NonNull String reportUri);

    @NonNull
    Observable<ReportOption> createReportOptionWithOverride(@NonNull String reportUri, @NonNull String label, @NonNull List<ReportParameter> params);

    @NonNull
    Observable<Void> deleteReportOption(@NonNull String uri, @NonNull String optionId);

    @NonNull
    Observable<List<InputControlState>> getReportOptionStates(@NonNull final String reportUri);
}
