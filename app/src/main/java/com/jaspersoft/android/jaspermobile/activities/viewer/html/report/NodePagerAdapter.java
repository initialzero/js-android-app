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