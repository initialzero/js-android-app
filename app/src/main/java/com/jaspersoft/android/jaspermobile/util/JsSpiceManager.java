package com.jaspersoft.android.jaspermobile.util;

import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.octo.android.robospice.SpiceManager;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class JsSpiceManager extends SpiceManager {
    public JsSpiceManager() {
        super(JsXmlSpiceService.class);
    }
}
