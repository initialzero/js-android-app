/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragment;
import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.RequestExecutor;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.SimpleRequestListener;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetInputControlsRequest;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlsList;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

import java.util.ArrayList;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EFragment
@OptionsMenu(R.menu.report_filter_manager_menu)
public class GetInputControlsFragment extends RoboSpiceFragment {

    public static final String TAG = GetInputControlsFragment.class.getSimpleName();

    @Inject
    protected JsRestClient jsRestClient;
    @Inject
    protected ReportParamsStorage paramsStorage;

    @FragmentArg
    protected String resourceUri;
    @OptionsMenuItem
    protected MenuItem showFilters;

    private OnInputControlsListener mListener = new NullListener();
    private ArrayList<InputControl> inputControls;
    private RequestExecutor requestExecutor;
    private boolean mShowFilterMenuItem, mLoading, mLoaded;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        setRetainInstance(true);

        if (activity instanceof OnInputControlsListener) {
            mListener = (OnInputControlsListener) activity;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        requestExecutor = RequestExecutor.builder()
                .setExecutionMode(RequestExecutor.Mode.VISIBLE)
                .setFragmentManager(getFragmentManager())
                .setSpiceManager(getSpiceManager())
                .create();
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!mLoading && !mLoaded) {
            mLoading = true;

            GetInputControlsRequest request =
                    new GetInputControlsRequest(jsRestClient, resourceUri);
            requestExecutor.execute(request, new GetInputControlsListener());
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        showFilters.setVisible(mShowFilterMenuItem);
    }

    @OptionsItem
    public void showFilters() {
        mListener.onShowControls();
    }

    private class GetInputControlsListener extends SimpleRequestListener<InputControlsList> {

        @Override
        protected Context getContext() {
            return getActivity();
        }

        @Override
        public void onRequestFailure(SpiceException exception) {
            super.onRequestFailure(exception);
            mLoading = false;
            mLoaded = false;
            ProgressDialogFragment.dismiss(getFragmentManager());
        }

        @Override
        public void onRequestSuccess(InputControlsList controlsList) {
            mLoading = false;
            mLoaded = true;

            inputControls = new ArrayList<InputControl>(controlsList.getInputControls());
            mShowFilterMenuItem = !inputControls.isEmpty();

            getActivity().supportInvalidateOptionsMenu();
            ProgressDialogFragment.dismiss(getFragmentManager());

            paramsStorage.putInputControls(resourceUri, inputControls);
            mListener.onLoaded();
        }
    }

    public static class NullListener implements OnInputControlsListener {
        @Override
        public void onLoaded() {
        }

        @Override
        public void onShowControls() {
        }
    }

    public static interface OnInputControlsListener {
        void onLoaded();
        void onShowControls();
    }
}
