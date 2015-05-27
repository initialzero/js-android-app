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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class NodePagerAdapter extends FragmentStatePagerAdapter {
    private final FragmentCreator<Fragment, Integer> mCreator;
    private int mCount;

    public NodePagerAdapter(FragmentManager fragmentManager, FragmentCreator<Fragment, Integer> creator) {
        super(fragmentManager);
        mCreator = creator;
    }

    public void addPage() {
        mCount++;
    }

    public void clear() {
        mCount = 0;
    }

    public void setCount(int count) {
        mCount = count;
    }

    @Override
    public Fragment getItem(int position) {
        return mCreator.createFragment(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + (position + 1);
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Parcelable saveState() {
        Bundle state = new Bundle();
        state.putInt("COUNT", mCount);
        return state;
    }

    @Override
    public void restoreState(Parcelable state, ClassLoader loader) {
        if (state != null) {
            Bundle bundle = (Bundle) state;
            mCount = bundle.getInt("COUNT");
            notifyDataSetChanged();
        }
    }
}