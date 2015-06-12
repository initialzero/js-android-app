/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.network;

import android.app.Application;
import android.util.Log;

import com.jaspersoft.android.jaspermobile.BuildConfig;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.springandroid.xml.SimpleSerializerObjectPersisterFactory;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;

import roboguice.util.temp.Ln;

/**
 * This class offers a {@link SpiceService} dedicated to xml web services. Provides
 * caching.
 *
 * @author Ivan Gadzhega
 * @since 1.6
 */
public class XmlSpiceService extends SpiceService {

    public XmlSpiceService() {
        super();
        if (!BuildConfig.DEBUG) {
            // Disable robospice logging
            Ln.getConfig().setLoggingLevel(Log.ERROR);
        }
    }

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        // It is really important to keep proper persister order.
        cacheManager.addPersister(new InFileStringObjectPersister(application));
        cacheManager.addPersister(new SimpleSerializerObjectPersisterFactory(application));
        return cacheManager;
    }

}