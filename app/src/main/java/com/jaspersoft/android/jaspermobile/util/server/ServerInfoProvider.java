package com.jaspersoft.android.jaspermobile.util.server;

import android.support.annotation.NonNull;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public interface ServerInfoProvider {
    @NonNull
    String getServerVersion();
}
