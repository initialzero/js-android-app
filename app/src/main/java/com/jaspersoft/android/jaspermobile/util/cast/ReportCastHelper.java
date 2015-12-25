/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.util.cast;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.Status;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.navigation.NavigationActivity_;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */

@EBean(scope = EBean.Scope.Singleton)
public class ReportCastHelper {

    @RootContext
    protected Context context;
    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private CastDevice mSelectedCastDevice;
    private BaseMediaRouterCallback mediaRouterCallback;
    private RouteCallbacks mRouteCallbacks;

    @AfterInject
    void init() {
        mMediaRouter = MediaRouter.getInstance(context);
        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory(CastMediaControlIntent.categoryForCast(context.getString(R.string.app_cast_id)))
                .build();
        mediaRouterCallback = new BaseMediaRouterCallback();
    }

    public void registerCallback(){
        mMediaRouter.addCallback(mMediaRouteSelector, mediaRouterCallback, MediaRouter.CALLBACK_FLAG_PERFORM_ACTIVE_SCAN);
    }

    public void unregisterCallback(){
        mMediaRouter.removeCallback(mediaRouterCallback);
    }

    public void applyRouteSelector(MediaRouteActionProvider mediaRouteActionProvider) {
        mediaRouteActionProvider.setRouteSelector(mMediaRouteSelector);
    }

    public void setRouteSelectListener(RouteCallbacks routeCallbacks){
        mRouteCallbacks = routeCallbacks;
    }

    public void startCastService(Activity context) {
        if (context == null || mSelectedCastDevice == null) return;

        CastRemoteDisplayLocalService.NotificationSettings emptySettings =
                new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                        .setNotificationPendingIntent(createEmptyIntent(context))
                        .build();

        CastRemoteDisplayLocalService.startService(context, ReportPresentationService.class, context.getString(R.string.app_cast_id),
                mSelectedCastDevice, emptySettings, new EmptyCastServiceCallback());
    }

    private void stopCastService() {
        CastRemoteDisplayLocalService.stopService();
    }

    private PendingIntent createEmptyIntent(Activity context) {
        Intent intent = NavigationActivity_.intent(context).get();
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return PendingIntent.getActivity(context, 0, intent, 0);
    }

    private class EmptyCastServiceCallback implements CastRemoteDisplayLocalService.Callbacks {
        @Override
        public void onServiceCreated(CastRemoteDisplayLocalService castRemoteDisplayLocalService) {

        }

        @Override
        public void onRemoteDisplaySessionStarted(CastRemoteDisplayLocalService castRemoteDisplayLocalService) {

        }

        @Override
        public void onRemoteDisplaySessionError(Status status) {

        }
    }

    private class BaseMediaRouterCallback extends MediaRouter.Callback {
        @Override
        public void onRouteSelected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteSelected(router, route);

            CastDevice castDevice = CastDevice.getFromBundle(route.getExtras());
            if (castDevice != null) {
                mSelectedCastDevice = castDevice;
                if (mRouteCallbacks != null) {
                    mRouteCallbacks.onRouteSelected();
                }
            }
        }

        @Override
        public void onRouteUnselected(MediaRouter router, MediaRouter.RouteInfo route) {
            super.onRouteUnselected(router, route);

            mSelectedCastDevice = null;
            stopCastService();
            if (mRouteCallbacks != null) {
                mRouteCallbacks.onRouteDeSelected();
            }
        }
    }

    public interface RouteCallbacks {
        void onRouteSelected();
        void onRouteDeSelected();
    }
}
