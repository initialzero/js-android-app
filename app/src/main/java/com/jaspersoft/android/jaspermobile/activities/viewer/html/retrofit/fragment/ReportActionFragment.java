package com.jaspersoft.android.jaspermobile.activities.viewer.html.retrofit.fragment;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
@OptionsMenu(R.menu.retrofit_report_menu)
public class ReportActionFragment extends Fragment {
    public static final String TAG = ReportActionFragment.class.getSimpleName();

    @FragmentArg
    ResourceLookup resource;
    @Bean
    FavoritesHelper favoritesHelper;

    @OptionsMenuItem
    MenuItem favoriteAction;
    @OptionsMenuItem
    MenuItem aboutAction;

    @InstanceState
    Uri favoriteEntryUri;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState == null) {
            favoriteEntryUri = favoritesHelper.queryFavoriteUri(resource);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        favoriteAction.setIcon(favoriteEntryUri == null ? R.drawable.ic_rating_not_favorite : R.drawable.ic_rating_favorite);
    }

    @OptionsItem
    final void favoriteAction() {
        favoriteEntryUri = favoritesHelper.
                handleFavoriteMenuAction(favoriteEntryUri, resource, favoriteAction);
    }

    @OptionsItem
    final void aboutAction() {
        SimpleDialogFragment.createBuilder(getActivity(), getFragmentManager())
                .setTitle(resource.getLabel())
                .setMessage(resource.getDescription())
                .show();
    }
}
