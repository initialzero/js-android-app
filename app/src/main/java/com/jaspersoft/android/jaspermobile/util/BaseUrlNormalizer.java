package com.jaspersoft.android.jaspermobile.util;

import android.support.annotation.Nullable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class BaseUrlNormalizer {
    private BaseUrlNormalizer() {}

    public static String normalize(@Nullable String url) {
        if (url == null || url.length() == 0) {
            return url;
        }
        if (!url.endsWith("/")) {
            return url + "/";
        }
        return url;
    }

    public static String denormalize(@Nullable String url) {
        if (url == null || url.length() == 0) {
            return url;
        }
        if (url.endsWith("/")) {
            return new String(url.substring(0, url.length() - 1));
        }
        return url;
    }
}
