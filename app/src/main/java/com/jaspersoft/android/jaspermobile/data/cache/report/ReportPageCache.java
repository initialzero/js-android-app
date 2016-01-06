package com.jaspersoft.android.jaspermobile.data.cache.report;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jaspersoft.android.jaspermobile.domain.ReportPage;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface ReportPageCache {
    @Nullable
    ReportPage get(@NonNull String uri, @NonNull String position);

    @NonNull
    ReportPage put(@NonNull String uri, @NonNull String position, @NonNull ReportPage content);

    void removePages(String uri);
}
