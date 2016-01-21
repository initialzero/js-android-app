package com.jaspersoft.android.jaspermobile.data.cache.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface VisualizeTemplateCache {
    @Nullable
    VisualizeTemplate get(@NonNull  Profile profile);

    @NonNull
    VisualizeTemplate put(@NonNull Profile profile, @NonNull VisualizeTemplate template);
}
