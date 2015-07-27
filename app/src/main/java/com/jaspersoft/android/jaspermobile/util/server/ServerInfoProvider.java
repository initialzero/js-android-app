package com.jaspersoft.android.jaspermobile.util.server;

import android.support.annotation.NonNull;
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
    /**
     * Returns current selected server organization. For instance it could be 'organization_1'
     *
     * @return empty string if user has not supplied organization
     */
    @NonNull
    String getOrganization();
    /**
     * This property identifies user on server side. Never changes and can be only deleted.
     *
     * @return user name identifier.
     */
    @NonNull
    String getUsername();
    /**
     * This property internal app identifier. For instance that could be name of account
     *
     * @return internal app identifier.
     */
    @NonNull
    String getAlias();
}
