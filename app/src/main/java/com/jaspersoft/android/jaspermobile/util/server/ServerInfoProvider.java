package com.jaspersoft.android.jaspermobile.util.server;

import android.support.annotation.Nullable;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public interface ServerInfoProvider {
    /**
     * Returns current selected server version. For instance it could be 5.5
     *
     * @return null if app is in consistent state otherwise current value
     */
    @Nullable
    String getServerVersion();
    /**
     * Returns current selected server version. For instance it could be CE or PRO
     *
     * @return null if app is in consistent state otherwise current value
     */
    @Nullable
    String getServerEdition();
}
