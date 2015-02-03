package com.jaspersoft.android.jaspermobile.activities.viewer.html.report;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment.NodeWebViewFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class NodePagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> fragments = new ArrayList<>();
    private final List<Integer> pages = new ArrayList<>();
    private final FragmentCreator<Fragment, Integer> mCreator;
    private int mCount;

    public NodePagerAdapter(FragmentManager fragmentManager, FragmentCreator<Fragment, Integer> creator) {
        super(fragmentManager);
        mCreator = creator;
    }

    public boolean containsPage(int page) {
        return pages.contains(page);
    }

    public void addPageOnDemand(int page) {
        if (!containsPage(page)) {
            addPage();
        }
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
        int page = position + 1;
        Fragment fragment = mCreator.createFragment(page);
        pages.add(page);
        fragments.add(fragment);
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return "Page " + (position + 1);
    }

    @Override
    public long getItemId(int position) {
        return position + 1;
    }

    @Override
    public int getItemPosition(Object object) {
        /*
         * Purpose of this method is to check whether an item in the adapter
         * still exists in the dataset and where it should show.
         * For each entry in dataset, request its Fragment.
         *
         * If the Fragment is found, return its (new) position. There's
         * no need to return POSITION_UNCHANGED; ViewPager handles it.
         *
         * If the Fragment passed to this method is not found, remove all
         * references and let the ViewPager remove it from display by
         * by returning POSITION_NONE;
         */
        NodeWebViewFragment fragment = (NodeWebViewFragment) object;
        if (fragments.contains(fragment)) {
            return fragments.indexOf(fragment);
        }

        // if we arrive here, the data-item for which the Fragment was created
        // does not exist anymore.
        // Let ViewPager remove the Fragment by returning POSITION_NONE.
        return POSITION_NONE;
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