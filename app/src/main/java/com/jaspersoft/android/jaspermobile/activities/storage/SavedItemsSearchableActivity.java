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

package com.jaspersoft.android.jaspermobile.activities.storage;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.repository.fragment.ResourcesControllerFragment_;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.jaspermobile.activities.storage.adapter.FileAdapter;
import com.jaspersoft.android.jaspermobile.activities.storage.fragment.SavedItemsControllerFragment;
import com.jaspersoft.android.jaspermobile.activities.storage.fragment.SavedItemsControllerFragment_;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;

import java.util.ArrayList;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EActivity
public class SavedItemsSearchableActivity extends RoboSpiceFragmentActivity {

    @Extra
    String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getString(R.string.search_result_format, query));
        }

        if (savedInstanceState == null) {
            SavedItemsControllerFragment savedItemsController =
                    SavedItemsControllerFragment_.builder()
                            .searchQuery(query)
                            .build();

            getSupportFragmentManager().beginTransaction()
                    .add(savedItemsController, ResourcesControllerFragment.TAG)
                    .commit();
        }

    }

    @OptionsItem(android.R.id.home)
    final void closeSearch() {
        super.onBackPressed();
    }

}
