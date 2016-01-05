package com.jaspersoft.android.jaspermobile.internal.di.modules;

import com.jaspersoft.android.sdk.network.Server;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public class ServerClientModule {
    private final String mBaseUrl;

    public ServerClientModule(String baseUrl) {
        mBaseUrl = appendPath(baseUrl);
    }

    @Provides
    Server provideServer() {
        return Server.builder()
                .withBaseUrl(mBaseUrl)
                .build();
    }

    private String appendPath(String url) {
        if ((url != null && url.length() > 0) && !url.endsWith("/")) {
            url += "/";
        }
        return url;
    }
}
