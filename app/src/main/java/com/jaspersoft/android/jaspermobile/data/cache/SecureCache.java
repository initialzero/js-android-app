package com.jaspersoft.android.jaspermobile.data.cache;

import com.jaspersoft.android.jaspermobile.domain.Profile;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface SecureCache {
    void put(Profile profile, String key, String value);
    String get(Profile profile, String key);
}
