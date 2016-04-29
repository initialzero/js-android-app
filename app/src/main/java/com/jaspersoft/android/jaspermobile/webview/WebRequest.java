package com.jaspersoft.android.jaspermobile.webview;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public interface WebRequest {
    @NonNull
    String getUrl();

    @Nullable
    String getMethod();

    @NonNull
    Map<String, String> getRequestHeaders();
}
