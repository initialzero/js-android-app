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

package com.jaspersoft.android.jaspermobile.data.repository.resource;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;

import com.jaspersoft.android.jaspermobile.data.entity.LoaderResult;
import com.jaspersoft.android.jaspermobile.data.loaders.LoaderFactory;
import com.jaspersoft.android.jaspermobile.data.loaders.SearchResourcesLoader;
import com.jaspersoft.android.jaspermobile.domain.loaders.ResourceLoader;
import com.jaspersoft.android.jaspermobile.internal.di.PerFragment;
import com.jaspersoft.android.sdk.service.data.repository.Resource;
import com.jaspersoft.android.sdk.service.repository.RepositorySearchCriteria;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@PerFragment
public class ResourceLoaderImpl implements ResourceLoader, LoaderManager.LoaderCallbacks<LoaderResult<List<Resource>>> {

    private final LoaderFactory mLoaderFactory;
    private final LoaderManager mLoaderManager;
    private LoaderCallback mLoaderCallback;
    private RepositorySearchCriteria mCriteria;
    private SearchResourcesLoader mSearchResourcesLoader;

    @Inject
    public ResourceLoaderImpl(
            LoaderFactory loaderFactory,
            LoaderManager loaderManager
    ) {
        mLoaderFactory = loaderFactory;
        mLoaderManager = loaderManager;
    }

    @NonNull
    @Override
    public void initSearch(@NonNull RepositorySearchCriteria criteria, LoaderCallback loaderCallback) {
        mLoaderCallback = loaderCallback;
        mCriteria = criteria;
        mSearchResourcesLoader = (SearchResourcesLoader) mLoaderManager.initLoader(242, null, this);
    }

    @NonNull
    @Override
    public void resetSearch(@NonNull RepositorySearchCriteria criteria) {
        mCriteria = criteria;
        mSearchResourcesLoader = (SearchResourcesLoader) mLoaderManager.restartLoader(242, null, this);
    }

    @NonNull
    @Override
    public void requestNext() {
        if (mSearchResourcesLoader.loadAvailable()) {
            mSearchResourcesLoader.forceLoad();
            mLoaderCallback.onLoadStarted();
        }
    }

    @Override
    public Loader<LoaderResult<List<Resource>>> onCreateLoader(int id, Bundle args) {
        return mLoaderFactory.createSearchResourceLoader(mCriteria);
    }

    @Override
    public void onLoadFinished(Loader<LoaderResult<List<Resource>>> loader, LoaderResult<List<Resource>> result) {
        if (result.hasResult()) {
            List<Resource> resourceList = result.getResult();
            mLoaderCallback.onLoaded(resourceList);
        } else {
            mLoaderCallback.onError(result.getServiceException());
        }
        if (((SearchResourcesLoader) loader).isLoading()) {
            mLoaderCallback.onLoadStarted();
        }
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<Resource>>> loader) {
    }
}
