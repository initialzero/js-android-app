/*
 * Copyright (c) 2014. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.info;

import android.text.TextUtils;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ServerInfoSnapshot {
    private final String edition;
    private final double versionCode;

    public ServerInfoSnapshot() {
        this.edition = null;
        this.versionCode = 0d;
    }

    public ServerInfoSnapshot(String edition, double versionCode) {
        this.edition = edition;
        this.versionCode = versionCode;
    }

    public String getEdition() {
        return edition;
    }

    public double getVersionCode() {
        return versionCode;
    }

    public boolean isServerInfoMissing() {
        return (TextUtils.isEmpty(edition) && versionCode == 0d);
    }
}
