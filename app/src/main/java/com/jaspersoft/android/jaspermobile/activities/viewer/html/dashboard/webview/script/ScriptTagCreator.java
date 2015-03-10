package com.jaspersoft.android.jaspermobile.activities.viewer.html.dashboard.webview.script;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface ScriptTagCreator {
    public static final String INJECTION_TOKEN = "**injection**";
    String createTag();
}
