package com.jaspersoft.android.jaspermobile.domain.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;
import com.jaspersoft.android.sdk.service.data.dashboard.DashboardControlComponent;

import java.util.List;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ControlsRepository {
    @NonNull
    Observable<List<InputControl>> listReportControls(@NonNull String reportUri);

    @NonNull
    Observable<List<InputControl>> listDashboardControls(@NonNull String reportUri);

    @NonNull
    Observable<List<InputControlState>> validateReportControls(@NonNull String reportUri);

    @NonNull
    Observable<List<InputControlState>> validateDashboardControls(String dashboardUri);

    @NonNull
    Observable<List<InputControlState>> listControlValues(@NonNull String reportUri);

    @NonNull
    Observable<List<DashboardControlComponent>> listDashboardControlComponents(@NonNull String dashboardUri);

    void flushControls(@NonNull String reportUri);

}
