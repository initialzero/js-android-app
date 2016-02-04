package com.jaspersoft.android.jaspermobile.data;

import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;
import com.squareup.okhttp.HttpUrl;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class ThumbNailGenerator {
    private final JasperServer mAppServer;

    @Inject
    public ThumbNailGenerator(JasperServer appServer) {
        mAppServer = appServer;
    }

    @NonNull
    public String generate(@NonNull String resourceUri) {
        String version = mAppServer.getVersion();
        ServerVersion serverVersion = ServerVersion.valueOf(version);
        if (serverVersion.greaterThanOrEquals(ServerVersion.v6)) {
            HttpUrl endpoint = HttpUrl.parse(mAppServer.getBaseUrl())
                    .newBuilder()
                    .addPathSegment("rest_v2")
                    .addPathSegment("thumbnails")
                    .build();
            HttpUrl resourceEndpoint = HttpUrl.parse(endpoint.toString() + resourceUri)
                    .newBuilder()
                    .addQueryParameter("defaultAllowed", "false")
                    .build();
            return resourceEndpoint.toString();
        }
        return "";
    }
}
