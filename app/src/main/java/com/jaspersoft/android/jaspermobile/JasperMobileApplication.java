/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile;

import android.app.Application;
import android.content.Context;
import android.view.ViewConfiguration;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.legacy.ProfileManager;
import com.jaspersoft.android.jaspermobile.uil.CustomImageDownaloder;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.retrofit.sdk.account.AccountProvider;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EApplication;

import java.lang.reflect.Field;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;
import timber.log.Timber;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
@EApplication
public class JasperMobileApplication extends Application {
    public static final String SAVED_REPORTS_DIR_NAME = "saved.reports";

    @Bean
    protected ProfileHelper profileHelper;
    @Inject
    protected JsRestClient jsRestClient;

    public static void removeAllCookies() {
        JsRestClient.flushCookies();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RoboInjector injector = RoboGuice.getInjector(this);
        injector.injectMembersWithoutViews(this);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.HollowTree());
        }

        // http://stackoverflow.com/questions/13182519/spring-rest-template-usage-causes-eofexception
        System.setProperty("http.keepAlive", "false");

        forceOverFlowMenu();
        initLegacyJsRestClient();
        initImageLoader(getApplicationContext());
    }

    private void initLegacyJsRestClient() {
        AccountProvider accountProvider = BasicAccountProvider.get(this);
        if (accountProvider.hasAccount()) {
            jsRestClient.setServerProfile(ProfileManager.getServerProfile(this));
        }
    }

    private void initImageLoader(Context context) {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(context)
                .imageDownloader(new CustomImageDownaloder(context))
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs() // Remove for release app
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
    }

    /**
     * We are forcing OS to show overflow menu for the devices which expose hardware implementation.
     * WARNING: This is considered to be bad practice though we decide to violate rules.
     * http://stackoverflow.com/questions/9286822/how-to-force-use-of-overflow-menu-on-devices-with-menu-button
     */
    private void forceOverFlowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if(menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }
    }

}