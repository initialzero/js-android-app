package com.jaspersoft.android.retrofit.sdk.util;

/**
 * SDK constants
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class JasperSettings {
    // Intent actions
    public static final String ACTION_AUTHORIZE = "jaspersoft.intent.action.AUTHORIZE";

    // Auth constants
    public static final String JASPER_ACCOUNT_TYPE = "com.jaspersoft";
    public static final String JASPER_AUTH_TOKEN_TYPE = "FULL ACCESS";

    // REST constants
    public static final String DEFAULT_REST_VERSION = "/rest_v2";

    private JasperSettings() {
        throw new RuntimeException();
    }

}
