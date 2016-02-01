package com.jaspersoft.android.jaspermobile.internal.di.modules.activity;

import com.jaspersoft.android.sdk.network.AnonymousClient;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.service.rx.auth.RxAuthorizationService;
import com.jaspersoft.android.sdk.service.rx.info.RxServerInfoService;

import java.net.CookieManager;

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

    @Provides
    AnonymousClient provideAnonymousClient(Server server) {
        return server.newClient()
                .withCookieHandler(CookieManager.getDefault())
                .create();
    }

    @Provides
    RxServerInfoService provideServerInfoService(AnonymousClient client) {
        return RxServerInfoService.newService(client);
    }

    @Provides
    RxAuthorizationService provideAuthorizationService(AnonymousClient client) {
        return RxAuthorizationService.newService(client);
    }

    private String appendPath(String url) {
        if ((url != null && url.length() > 0) && !url.endsWith("/")) {
            url += "/";
        }
        return url;
    }
}
