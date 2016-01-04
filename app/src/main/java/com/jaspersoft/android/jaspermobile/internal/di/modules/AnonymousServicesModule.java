package com.jaspersoft.android.jaspermobile.internal.di.modules;

import com.jaspersoft.android.sdk.network.AnonymousClient;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.service.rx.auth.RxAuthorizationService;
import com.jaspersoft.android.sdk.service.rx.info.RxServerInfoService;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public class AnonymousServicesModule {
    @Provides
    AnonymousClient provideAnonymousClient(Server server, CookieStore cookieStore) {
        CookieManager cookieHandler = new CookieManager(cookieStore, CookiePolicy.ACCEPT_ORIGINAL_SERVER);
        return server.newClient()
                .withCookieHandler(cookieHandler)
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
}
