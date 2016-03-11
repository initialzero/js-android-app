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

package com.jaspersoft.android.jaspermobile.presentation.presenter;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.CriteriaMapper;
import com.jaspersoft.android.jaspermobile.data.entity.mapper.ResourceMapper;
import com.jaspersoft.android.jaspermobile.domain.loaders.ResourceLoader;
import com.jaspersoft.android.jaspermobile.internal.di.PerFragment;
import com.jaspersoft.android.jaspermobile.presentation.contract.LibraryContract;
import com.jaspersoft.android.jaspermobile.util.filtering.LibraryResourceFilter_;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.service.data.repository.Resource;
import com.jaspersoft.android.sdk.service.exception.ServiceException;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@PerFragment
public class LibraryPresenter extends Presenter<LibraryContract.View> implements LibraryContract.ActionListener, ResourceLoader.LoaderCallback {

    public static final String ROOT_URI = "/";

    @Inject
    Context mContext;
    @Inject
    CriteriaMapper mCriteriaMapper;
    @Inject
    ResourceMapper mResourceMapper;
    @Inject
    ResourceLoader mResourceLoader;
    @Inject
    @Named("LIMIT")
    int mLimit;

    private ResourceLookupSearchCriteria mSearchCriteria;

    @Inject
    public LibraryPresenter(ResourceLoader resourceLoader) {
        mResourceLoader = resourceLoader;
    }

    @Override
    public void onReady() {
        mSearchCriteria = new ResourceLookupSearchCriteria();
        mSearchCriteria.setForceFullPage(true);
        mSearchCriteria.setLimit(15);
        mSearchCriteria.setRecursive(true);
        mSearchCriteria.setTypes(LibraryResourceFilter_.getInstance_(mContext).getCurrent().getValues());
        mSearchCriteria.setFolderUri(ROOT_URI);
//        if (!TextUtils.isEmpty(query)) {
//            mSearchCriteria.setQuery(query);
//        }
//        if (sortOrder != null) {
//            mSearchCriteria.setSortBy(sortOrder.getValue());
//        }

        getView().showFirstLoading();
        mResourceLoader.initSearch(mCriteriaMapper.toRetrofittedCriteria(mSearchCriteria), this);
    }

    @Override
    public void onRefresh() {
        reloadResources();
    }

    @Override
    public void onScrollToEnd() {
        mResourceLoader.requestNext();
    }

    @Override
    public void onLoadStarted() {
        getView().showNextLoading();
    }

    @Override
    public void onLoaded(List<Resource> result) {
        List<JasperResource> jasperResources = mResourceMapper.toJasperResources(result);
        getView().showResources(jasperResources);
        getView().hideLoading();
    }

    @Override
    public void onError(ServiceException ex) {

    }

    private void reloadResources() {
        getView().clearResources();
        getView().showFirstLoading();
        mResourceLoader.resetSearch(mCriteriaMapper.toRetrofittedCriteria(mSearchCriteria));
    }
}
