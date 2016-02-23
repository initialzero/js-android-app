package com.jaspersoft.android.jaspermobile.data.cache.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ControlsCache {
    @NonNull
    List<InputControl> put(@NonNull String uri, @NonNull List<InputControl> controls);

    @Nullable
    List<InputControl> get(@NonNull String uri);

    void evict(String reportUri);
}
