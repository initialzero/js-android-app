package com.jaspersoft.android.jaspermobile.activities.storage.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.jaspersoft.android.jaspermobile.util.ControllerFragment;

import org.androidannotations.annotations.EFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class SavedItemsControllerFragment extends ControllerFragment {

    public static final String TAG = SavedItemsControllerFragment.class.getSimpleName();

    private SavedItemsFragment contentFragment;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SavedItemsFragment inMemoryFragment = (SavedItemsFragment)
                getFragmentManager().findFragmentByTag(CONTENT_TAG);

        if (inMemoryFragment == null) {
            commitContentFragment();
        } else {
            contentFragment = inMemoryFragment;
        }

    }

    @Override
    public Fragment getContentFragment() {
        contentFragment = SavedItemsFragment_.builder()
                .viewType(getViewType()).build();
        return contentFragment;
    }

}
