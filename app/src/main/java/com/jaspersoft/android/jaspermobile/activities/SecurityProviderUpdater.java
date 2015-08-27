package com.jaspersoft.android.jaspermobile.activities;

import android.content.Context;

import com.google.android.gms.security.ProviderInstaller;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public interface SecurityProviderUpdater {
    void update(Context context, ProviderInstaller.ProviderInstallListener listener);
}
