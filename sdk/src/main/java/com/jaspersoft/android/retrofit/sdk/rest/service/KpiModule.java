package com.jaspersoft.android.retrofit.sdk.rest.service;

import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface KpiModule {
    @Headers({"Content-Type: application/repository.file+json"})
    @GET("/resources/kpicache.properties")
    Response getKpiCache(@Header("Cookie") String cookie);
}
