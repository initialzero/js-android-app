package com.jaspersoft.android.jaspermobile.activities.info;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.ResourceViewHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@OptionsMenu(R.menu.favorite_menu)
@EFragment(R.layout.fragment_resource_info)
public class ResourceInfoFragment extends SimpleInfoFragment {

    @Inject
    protected JsRestClient jsRestClient;

    @Bean
    protected FavoritesHelper favoriteHelper;

    @OptionsMenuItem(R.id.favoriteAction)
    protected MenuItem favoriteAction;

    private ResourceViewHelper viewHelper;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewHelper = new ResourceViewHelper(getActivity());
        viewHelper.populateView(new InfoHeaderView(toolbarImage, toolbarLayout), resourceLookup);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        alterFavoriteIcon();
    }

    @OptionsItem(R.id.favoriteAction)
    final void favoriteAction() {
        Uri uri = favoriteHelper.queryFavoriteUri(resourceLookup);
        favoriteHelper.handleFavoriteMenuAction(uri, resourceLookup, favoriteAction);
    }

    private void alterFavoriteIcon() {
        Cursor cursor = favoriteHelper.queryFavoriteByResource(resourceLookup);

        try {
            boolean alreadyFavorite = (cursor.getCount() > 0);
            favoriteAction.setIcon(alreadyFavorite ? R.drawable.ic_menu_star : R.drawable.ic_menu_star_outline);
            favoriteAction.setTitle(alreadyFavorite ? R.string.r_cm_remove_from_favorites : R.string.r_cm_add_to_favorites);
        } finally {
            if (cursor != null) cursor.close();
        }
    }
}
