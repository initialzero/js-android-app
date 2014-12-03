/*
 * Copyright (c) 2014. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.info;

import android.text.TextUtils;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class ServerInfoSnapshot {
    private String edition;
    private double versionCode;

    @Inject
    public ServerInfoSnapshot() {
        this.edition = null;
        this.versionCode = 0d;
    }

    public boolean isAmberRelease() {
        return versionCode >= ServerInfo.VERSION_CODES.AMBER;
    }

    public String getEdition() {
        return edition;
    }

    public double getVersionCode() {
        return versionCode;
    }

    public boolean isMissing() {
        return (TextUtils.isEmpty(edition) && versionCode == 0d);
    }

    public void setProfile(ServerProfiles profile) {
        setEdition(profile.getEdition());
        setVersionCode(profile.getVersioncode());
    }

    public void setEdition(String edition) {
        this.edition = edition;
    }

    public void setVersionCode(double versionCode) {
        this.versionCode = versionCode;
    }

}
