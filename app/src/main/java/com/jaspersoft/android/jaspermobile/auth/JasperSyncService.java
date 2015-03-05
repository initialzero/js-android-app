/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.auth;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class JasperSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static JasperSyncAdapter sJasperSyncAdapter = null;

    @Override
    public void onCreate() {
        synchronized (sSyncAdapterLock) {
            if (sJasperSyncAdapter == null) {
                sJasperSyncAdapter = new JasperSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sJasperSyncAdapter.getSyncAdapterBinder();
    }
}
