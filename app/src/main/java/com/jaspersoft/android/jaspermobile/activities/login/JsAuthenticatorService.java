package com.jaspersoft.android.jaspermobile.activities.login;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EService;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
@EService
public class JsAuthenticatorService extends Service {
    @Bean
    JsAuthenticator authenticator;

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
