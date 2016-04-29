package com.jaspersoft.android.jaspermobile.webview;

import java.io.InputStream;
import java.util.Map;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public interface WebResponse {
    String getMimeType();

    String getEncoding();

    InputStream getData();

    int getStatusCode();

    String getReasonPhrase();

    Map<String, String> getResponseHeaders();
}
