package com.jaspersoft.android.jaspermobile.util;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.RepositoryPref_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.activities.settings.SettingsActivity;

import roboguice.fragment.RoboFragment;

import static com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType.GRID;
import static com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType.LIST;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public abstract class ControllerFragment extends RoboFragment {

    public static final String CONTENT_TAG = "CONTENT_TAG";

    private MenuItem switchLayoutMenuItem;
    private RepositoryPref_ repositoryPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        repositoryPref = new RepositoryPref_(getActivity());
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.switch_menu, menu);
        switchLayoutMenuItem = menu.findItem(R.id.switchLayout);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        toggleSwitcher();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled = super.onOptionsItemSelected(item);
        if (handled) {
            return true;
        }
        int itemId_ = item.getItemId();
        if (itemId_ == R.id.switchLayout) {
            switchLayout();
            return true;
        }
        return false;
    }

    private void switchLayout() {
        repositoryPref.viewType()
                .put(getViewType() == LIST ? GRID.toString() : LIST.toString());
        toggleSwitcher();
        commitContentFragment();
        getActivity().invalidateOptionsMenu();
    }

    private void toggleSwitcher() {
        if (getViewType() == LIST) {
            switchLayoutMenuItem.setIcon(R.drawable.ic_collections_view_as_grid);
        } else {
            switchLayoutMenuItem.setIcon(R.drawable.ic_collections_view_as_list);
        }
    }

    protected void commitContentFragment() {
        boolean animationEnabled = SettingsActivity.isAnimationEnabled(getActivity());
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        if (animationEnabled) {
            transaction.setCustomAnimations(R.anim.fade_in, R.anim.fade_out);
        }
        transaction
                .replace(android.R.id.content, getContentFragment(), CONTENT_TAG)
                .commit();
    }

    protected ViewType getViewType() {
        if (repositoryPref == null) {
            repositoryPref = new RepositoryPref_(getActivity());
        }
        return ViewType.valueOf(repositoryPref);
    }

    public abstract Fragment getContentFragment();
}
