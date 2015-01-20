package com.jaspersoft.android.jaspermobile.network;

import android.app.Application;

import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.persistence.CacheManager;
import com.octo.android.robospice.persistence.exception.CacheCreationException;
import com.octo.android.robospice.persistence.springandroid.xml.SimpleSerializerObjectPersisterFactory;
import com.octo.android.robospice.persistence.string.InFileStringObjectPersister;

/**
 * This class offers a {@link SpiceService} dedicated to xml web services. Provides
 * caching.
 *
 * @author Ivan Gadzhega
 * @since 1.6
 */
public class XmlSpiceService extends SpiceService {

    @Override
    public CacheManager createCacheManager(Application application) throws CacheCreationException {
        CacheManager cacheManager = new CacheManager();
        // It is really important to keep proper persister order.
        cacheManager.addPersister(new InFileStringObjectPersister(application));
        cacheManager.addPersister(new SimpleSerializerObjectPersisterFactory(application));
        return cacheManager;
    }

}