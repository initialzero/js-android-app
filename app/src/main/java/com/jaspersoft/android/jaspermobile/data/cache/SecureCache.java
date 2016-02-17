package com.jaspersoft.android.jaspermobile.data.cache;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface SecureCache {
    void reset();
    void put(String key, String value);
    String get(String key);
}
