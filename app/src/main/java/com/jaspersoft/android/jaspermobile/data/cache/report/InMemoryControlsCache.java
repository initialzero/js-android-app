package com.jaspersoft.android.jaspermobile.data.cache.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryControlsCache implements ControlsCache {
    private final ReportParamsStorage mParamsStorage;

    @Inject
    public InMemoryControlsCache(ReportParamsStorage paramsStorage) {
        mParamsStorage = paramsStorage;
    }

    @NonNull
    @Override
    public List<InputControl> put(@NonNull String uri, @NonNull List<InputControl> controls) {
        mParamsStorage.getInputControlHolder(uri).setInputControls(controls);
        return controls;
    }

    @Nullable
    @Override
    public List<InputControl> get(@NonNull String uri) {
        return mParamsStorage.getInputControlHolder(uri).getInputControls();
    }
}
