package com.jaspersoft.android.jaspermobile.domain.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlState;

import java.util.List;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ControlsRepository {
    @NonNull
    Observable<List<InputControl>> listControls(@NonNull String reportUri);

    @NonNull
    Observable<List<InputControlState>> validateControls(@NonNull String reportUri);

    void flushControls(@NonNull String reportUri);
}
