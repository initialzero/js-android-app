package com.jaspersoft.android.retrofit.sdk.rest.service;

import com.jaspersoft.android.retrofit.sdk.ojm.Kpi;

import java.util.Collection;

import retrofit.Callback;
import retrofit.client.Response;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.Path;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface KpiModule {
    @Headers({"Content-Type: application/repository.file+json"})
    @GET("/resources/kpicache.properties")
    Response getKpiCache(@Header("Cookie") String cookie);

    @Headers({"Accept: application/json"})
    @GET("/reports{kpiUri}")
    void getKpi(@Header("Cookie") String cookie,
                @Path(value = "kpiUri", encode = false) String kpiUri,
                Callback<Collection<Kpi>> object);
}
