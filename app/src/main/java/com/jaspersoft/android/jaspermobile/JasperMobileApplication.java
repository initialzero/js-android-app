/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile;

import android.app.Application;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.legacy.JsServerProfileCompat;
import com.jaspersoft.android.jaspermobile.network.TokenImageDownloader;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.L;

import org.androidannotations.annotations.EApplication;

import roboguice.RoboGuice;
import timber.log.Timber;

/**
 * @author Ivan Gadzhega
 * @since 1.0
 */
@EApplication
public class JasperMobileApplication extends Application {
    public static final String SAVED_REPORTS_DIR_NAME = "saved.reports";
    public static final String RESOURCES_CACHE_DIR_NAME = "resources";
    public static final String SHARED_DIR = "com.jaspersoft.account.none";

    @Inject
    AppConfigurator appConfigurator;
    @Inject
    JsRestClient jsRestClient;
    @Inject
    Analytics analytics;

    @Override
    public void onCreate() {
        super.onCreate();
        RoboGuice.getInjector(this).injectMembers(this);
        initLegacyJsRestClient();

        forceDatabaseUpdate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new Timber.HollowTree());
        }

        // http://stackoverflow.com/questions/13182519/spring-rest-template-usage-causes-eofexception
        System.setProperty("http.keepAlive", "false");

        analytics.init(this);
        appConfigurator.configCrashAnalytics(this);
        initImageLoader();
    }

    private void forceDatabaseUpdate() {
        getContentResolver().query(MobileDbProvider.FAVORITES_CONTENT_URI, new String[]{"_id"}, null, null, null);
    }

    public void initLegacyJsRestClient() {
        JsServerProfileCompat.initLegacyJsRestClient(this, jsRestClient);
    }

    private void initImageLoader() {
        // This configuration tuning is custom. You can tune every option, you may tune some of them,
        // or you can create default configuration by
        //  ImageLoaderConfiguration.createDefault(this);
        // method.
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .imageDownloader(new TokenImageDownloader(this))
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .diskCacheFileNameGenerator(new Md5FileNameGenerator())
                .diskCacheSize(50 * 1024 * 1024) // 50 Mb
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .build();
        // Initialize ImageLoader with configuration.
        ImageLoader.getInstance().init(config);
        // Ignoring all log from UIL
        L.writeLogs(false);
    }
}