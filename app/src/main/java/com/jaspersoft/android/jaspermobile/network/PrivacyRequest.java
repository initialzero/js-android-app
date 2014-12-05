package com.jaspersoft.android.jaspermobile.network;

import com.google.inject.Inject;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.octo.android.robospice.request.springandroid.SpringAndroidSpiceRequest;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
public class PrivacyRequest extends SpringAndroidSpiceRequest<String> {
    public static final String PRIVACY_URL = "http://www.tibco.com/company/privacy-cma";

    @Inject
    private JsRestClient jsRestClient;

    @Inject
    public PrivacyRequest() {
        super(String.class);
    }

    @Override
    public String loadDataFromNetwork() throws Exception {
        return jsRestClient.getRestTemplate().getForObject(PRIVACY_URL, String.class);
    }

    public String createCacheKey() {
        return PRIVACY_URL;
    }
}