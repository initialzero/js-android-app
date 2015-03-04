package com.jaspersoft.android.jaspermobile.activities.viewer.html.webview;

import org.androidannotations.annotations.sharedpreferences.SharedPref;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@SharedPref
public interface ScalePref {
    int pageScale();
    String pageSize();
}
