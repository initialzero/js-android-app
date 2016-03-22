package com.jaspersoft.android.jaspermobile.data.cache.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class InMemoryVisualizeTemplateCache implements VisualizeTemplateCache {
    private final Map<Profile, VisualizeTemplate> mCache = new HashMap<>();

    @Inject
    public InMemoryVisualizeTemplateCache() {
    }

    @Nullable
    @Override
    public VisualizeTemplate get(@NonNull Profile profile) {
        return mCache.get(profile);
    }

    @NonNull
    @Override
    public VisualizeTemplate put(@NonNull Profile profile, @NonNull VisualizeTemplate template) {
        return mCache.put(profile, template);
    }
}
