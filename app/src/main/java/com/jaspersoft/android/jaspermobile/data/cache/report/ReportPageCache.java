package com.jaspersoft.android.jaspermobile.data.cache.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportPageCache {
    @Nullable
    ReportPage get(@NonNull PageRequest pageRequest);

    @NonNull
    ReportPage put(@NonNull PageRequest pageRequest, @NonNull ReportPage content);

    void evictAll();
}
