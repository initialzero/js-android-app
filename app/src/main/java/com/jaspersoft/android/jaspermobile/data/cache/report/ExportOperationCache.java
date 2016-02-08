package com.jaspersoft.android.jaspermobile.data.cache.report;

import android.net.Uri;
import android.os.AsyncTask;

import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class ExportOperationCache {
    private final Map<Uri, AsyncTask<?, ?, ?>> mCache = new HashMap<>();

    @Inject
    public ExportOperationCache() {
    }

    public void add(Uri key, AsyncTask<?, ?, ?> operation) {
        mCache.put(key, operation);
    }

    public void remove(Uri key) {
        mCache.remove(key);
    }

    public AsyncTask<?, ?, ?> get(Uri current) {
        return mCache.get(current);
    }
}
