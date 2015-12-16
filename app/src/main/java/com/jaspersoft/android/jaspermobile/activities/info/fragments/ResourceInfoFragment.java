package com.jaspersoft.android.jaspermobile.activities.info.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;
import org.androidannotations.annotations.ViewById;

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        mJasperResourceConverter = new JasperResourceConverter(getActivity());

        requestInfo();
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

    private void requestInfo() {
        final GetResourceDescriptorRequest request = new GetResourceDescriptorRequest(jsRestClient, jasperResource.getId(),
                mJasperResourceConverter.convertToResourceType(jasperResource.getResourceType()));
        getSpiceManager().execute(request, new GetResourceDescriptorListener());

        ProgressDialogFragment.builder(getFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        getActivity().finish();
                    }
                })
                .show();
    }

    final protected void fillWithData() {
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
        public void onRequestSuccess(ResourceLookup resourceLookup) {
            mResourceLookup = resourceLookup;
            jasperResource.setLabel(resourceLookup.getLabel());

            fillWithData();
            updateHeaderView();

            if (favoriteAction != null) {
                alterFavoriteIcon();
            }

            ProgressDialogFragment.dismiss(getFragmentManager());
        }
    }
}
