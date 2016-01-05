/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.robospice;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.app.MediaRouteDialogFactory;
import android.support.v7.media.MediaRouteProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.Status;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.util.cast.ResourceCastDialogFactory;
import com.jaspersoft.android.jaspermobile.util.cast.ResourcePresentationService;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.OptionsMenuItem;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EActivity
@OptionsMenu(R.menu.cast_menu)
public abstract class RoboCastActivity extends RoboToolbarActivity {

    @OptionsMenuItem(R.id.castReport)
    protected MenuItem castAction;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private CastDevice mSelectedCastDevice;
    private MediaRouter.Callback mMediaRouterCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mMediaRouter = MediaRouter.getInstance(getApplicationContext());
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(getString(R.string.app_cast_id)))
                .build();
        mMediaRouterCallback = new BaseMediaRouterCallback();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);

        if (castAction != null) {
            MediaRouteActionProvider mediaRouteActionProvider =
                    (MediaRouteActionProvider) MenuItemCompat.getActionProvider(castAction);
            mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
            mediaRouteActionProvider.setDialogFactory(new ResourceCastDialogFactory());
            return true;
        }
        return result;
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMediaRouter.addCallback(mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    @Override
    protected void onStop() {
        mMediaRouter.removeCallback(mMediaRouterCallback);
        super.onStop();
    }

    protected void onCastStarted() {
    }

    protected void onCastStopped() {
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private void startCastService() {
        if (mSelectedCastDevice == null) return;

        CastRemoteDisplayLocalService.NotificationSettings emptySettings =
                new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                        .setNotificationPendingIntent(createEmptyIntent(this))
                        .build();

        CastRemoteDisplayLocalService.startService(this, ResourcePresentationService.class, getString(R.string.app_cast_id),
                mSelectedCastDevice, emptySettings, new CastServiceCallback());
    }

    private PendingIntent createEmptyIntent(Activity context) {
        Intent intent = NavigationActivity_.intent(context).get();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    //---------------------------------------------------------------------
    // Nested classes
    //---------------------------------------------------------------------

    private final class BaseMediaRouterCallback extends MediaRouter.Callback {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteSelected(router, route);

            CastDevice castDevice = CastDevice.getFromBundle(route.getExtras());
            if (castDevice != null) {
                mSelectedCastDevice = castDevice;
                startCastService();
            }
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteUnselected(router, route);

            mSelectedCastDevice = null;
            CastRemoteDisplayLocalService.stopService();
            onCastStopped();
        }
    }

    private final class CastServiceCallback implements CastRemoteDisplayLocalService.Callbacks {
        @Override
        public void onServiceCreated(CastRemoteDisplayLocalService castRemoteDisplayLocalService) {
            onCastStarted();
        }

        @Override
        public void onRemoteDisplaySessionStarted(CastRemoteDisplayLocalService castRemoteDisplayLocalService) {

        }

        @Override
        public void onRemoteDisplaySessionError(Status status) {
            Toast.makeText(RoboCastActivity.this, "Error casting " + status, Toast.LENGTH_SHORT).show();
        }
    }
}
