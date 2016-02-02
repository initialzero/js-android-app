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

package com.jaspersoft.android.jaspermobile.activities.info.fragments;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EFragment;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EFragment(R.layout.fragment_resource_info)
public class FileInfoFragment extends ResourceInfoFragment {

    @Override
    protected void onDataObtain() {
        super.onDataObtain();

        loadFileType();
    }

    private void loadFileType() {
        // TODO list file
//        GetFileResourceRequest mFileResourceRequest = new GetFileResourceRequest(jsRestClient, jasperResource.getId());
//        getSpiceManager().execute(mFileResourceRequest, new SimpleRequestListener<FileLookup>() {
//            @Override
//            protected Context getContext() {
//                return getActivity();
//            }
//
//            @Override
//            public void onRequestSuccess(FileLookup fileLookup) {
//                infoView.addInfoItem(getString(R.string.ri_file_format), fileLookup.getFileType().name(), 1);
//            }
//        });
    }

}
