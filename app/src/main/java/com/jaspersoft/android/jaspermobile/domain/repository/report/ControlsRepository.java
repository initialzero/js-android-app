package com.jaspersoft.android.jaspermobile.domain.repository.report;

import android.support.annotation.NonNull;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.List;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ControlsRepository {
    @NonNull
    Observable<List<InputControl>> listControls(@NonNull String reportUri);

    void flushControls(@NonNull String reportUri);
}
