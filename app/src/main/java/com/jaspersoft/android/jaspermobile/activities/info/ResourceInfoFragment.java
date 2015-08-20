package com.jaspersoft.android.jaspermobile.activities.info;

import android.accounts.Account;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.util.account.AccountServerData;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsMenu;

import roboguice.inject.InjectView;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EFragment(R.layout.fragment_resource_info)
@OptionsMenu(R.menu.am_resource_info_menu)
public class ResourceInfoFragment extends RoboSpiceFragment {

    public static final String TAG = ResourceInfoFragment.class.getSimpleName();

    @FragmentArg
    protected ResourceLookup resourceLookup;

    @InjectView(R.id.toolbarImageView)
    protected TopCropImageView toolbarImage;

    @InjectView(R.id.info_collapsing_toolbar)
    protected CollapsingToolbarLayout toolbarLayout;

    @Inject
    protected JsRestClient jsRestClient;

    private  boolean isAmberOrHigher;

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Account account = JasperAccountManager.get(getActivity()).getActiveAccount();
        AccountServerData serverData = AccountServerData.get(getActivity(), account);
        ServerRelease serverRelease = ServerRelease.parseVersion(serverData.getVersionName());
        isAmberOrHigher = serverRelease.code() >= ServerRelease.AMBER.code();

        setToolbar(view);
        toolbarLayout.setTitle(resourceLookup.getLabel());
        showImage();
    }

    private void setToolbar(View infoView) {
        Toolbar toolbar = (Toolbar) infoView.findViewById(R.id.toolbar);

        ActionBarActivity actionBarActivity = (ActionBarActivity) getActivity();
        actionBarActivity.setSupportActionBar(toolbar);

        ActionBar actionBar = actionBarActivity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_close);
        }
    }

    private void showImage(){
        loadFromNetwork(toolbarImage, resourceLookup.getUri());
        toolbarImage.setScaleType(TopCropImageView.ScaleType.TOP_CROP);
        toolbarImage.setBackgroundResource(R.drawable.bg_gradient_blue);
    }

    private void loadFromNetwork(ImageView imageView, String uri) {
        String path = "";
        if (isAmberOrHigher) {
            path = jsRestClient.generateThumbNailUri(uri);
        }
        ImageLoader.getInstance().displayImage(
                path, imageView, getDisplayImageOptions()
        );
    }

    private DisplayImageOptions getDisplayImageOptions() {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.placeholder_report)
                .showImageForEmptyUri(R.drawable.placeholder_report)
                .considerExifParams(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

}
