package com.jaspersoft.android.jaspermobile.activities.info.fragments;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.FavoritesHelper;
import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceConverter;
import com.jaspersoft.android.jaspermobile.widget.InfoView;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.GetResourceDescriptorRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.util.FileUtils;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;

import java.io.File;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@OptionsMenu(R.menu.favorite_menu)
@EFragment(R.layout.fragment_resource_info)
public class ResourceInfoFragment extends SimpleInfoFragment {

    @ViewById(R.id.infoDetailsView)
    protected InfoView infoView;

    @Inject
    protected JsRestClient jsRestClient;

    @Bean
    protected FavoritesHelper favoriteHelper;

    @OptionsMenuItem(R.id.favoriteAction)
    protected MenuItem favoriteAction;

    private JasperResourceConverter mJasperResourceConverter;

    protected ResourceLookup mResourceLookup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mJasperResourceConverter = new JasperResourceConverter(getActivity());
    }

    @AfterViews
    protected void requestInfo() {
        final GetResourceDescriptorRequest request = new GetResourceDescriptorRequest(jsRestClient, jasperResource.getId(),
                mJasperResourceConverter.convertToResourceType(jasperResource.getResourceType()));
        getSpiceManager().execute(request, new GetResourceDescriptorListener());
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (mResourceLookup != null) {
            alterFavoriteIcon();
        }
    }

    @OptionsItem(R.id.favoriteAction)
    final void favoriteAction() {
        Uri uri = favoriteHelper.queryFavoriteUri(mResourceLookup);
        favoriteHelper.handleFavoriteMenuAction(uri, mResourceLookup, favoriteAction);
    }

    private void alterFavoriteIcon() {
        Cursor cursor = favoriteHelper.queryFavoriteByResource(mResourceLookup);

        try {
            boolean alreadyFavorite = (cursor.getCount() > 0);
            favoriteAction.setIcon(alreadyFavorite ? R.drawable.ic_menu_star : R.drawable.ic_menu_star_outline);
            favoriteAction.setTitle(alreadyFavorite ? R.string.r_cm_remove_from_favorites : R.string.r_cm_add_to_favorites);
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    private void fillWithData() {
        infoView.fillWithBaseData(mResourceLookup.getResourceType().name(), mResourceLookup.getLabel(),
                mResourceLookup.getDescription(), mResourceLookup.getUri(),
                mResourceLookup.getCreationDate(), mResourceLookup.getUpdateDate());
    }

    private class GetResourceDescriptorListener extends SimpleRequestListener<ResourceLookup> {

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException spiceException) {
            super.onRequestFailure(spiceException);

            ProgressDialogFragment.dismiss(getFragmentManager());
            getActivity().finish();
        }

        @Override
        public void onRequestSuccess(final ResourceLookup resourceLookup) {
            mResourceLookup = resourceLookup;
            jasperResource.setLabel(resourceLookup.getLabel());

            fillWithData();
            updateHeaderViewLabel(resourceLookup.getLabel());

            if (favoriteAction != null) {
                alterFavoriteIcon();
            }
        }
    }
}
