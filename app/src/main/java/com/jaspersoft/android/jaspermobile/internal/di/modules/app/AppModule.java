package com.jaspersoft.android.jaspermobile.internal.di.modules.app;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.BackgroundThread;
import com.jaspersoft.android.jaspermobile.JasperAnalytics;
import com.jaspersoft.android.jaspermobile.UIThread;
import com.jaspersoft.android.jaspermobile.data.cache.SecureCache;
import com.jaspersoft.android.jaspermobile.data.cache.SecureStorage;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.jaspersoft.android.jaspermobile.legacy.JsRestClientWrapper;
import com.jaspersoft.android.jaspermobile.network.cookie.CookieStorage;
import com.jaspersoft.android.jaspermobile.network.cookie.CookieStorageFactory;
import com.jaspersoft.android.jaspermobile.util.JasperSettings;

import java.net.CookieStore;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class
AppModule {
    private final Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    @ApplicationContext
    Context provideApplicationContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    CookieStorage provideSessionCache(CookieStorageFactory cookieStorageFactory, @ApplicationContext Context context) {
        return cookieStorageFactory.newStore(context);
    }

    @Provides
    @Singleton
    CookieStore provideCookieStore(CookieStorage appCookieStore) {
        return appCookieStore;
    }

    @Provides
    @Singleton
    PostExecutionThread providePostExecutionThread(UIThread uiThread) {
        return uiThread;
    }

    @Provides
    @Singleton
    PreExecutionThread providePreExecutionThread(BackgroundThread backgroundThread) {
        return backgroundThread;
    }

    @Provides
    @Singleton
    @Named("accountType")
    String provideAccountType() {
        return JasperSettings.JASPER_ACCOUNT_TYPE;
    }

    @Provides
    @Singleton
    AccountManager providesAccountManager(@ApplicationContext Context context) {
        return AccountManager.get(context);
    }

    @Provides
    @Singleton
    JsRestClientWrapper providesJsRestClientWrapper(@ApplicationContext Context context, CookieStorage cookieStore) {
        return new JsRestClientWrapper(context, cookieStore);
    }

    @Provides
    @Singleton
    Analytics providesAnalytics(@ApplicationContext Context context) {
        return new JasperAnalytics(context);
    }

    @Provides
    @Singleton
    SecureCache provideSecureStorage(SecureStorage secureStorage) {
        return secureStorage;
    }
}
