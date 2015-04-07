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

package com.jaspersoft.android.jaspermobile.test;

import android.accounts.Account;
import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.support.test.InstrumentationRegistry;
import android.support.test.internal.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.Stage;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.WindowManager;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.jaspersoft.android.jaspermobile.test.utils.AccountUtil;
import com.jaspersoft.android.jaspermobile.test.utils.pref.PreferenceApiAdapter;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;

import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collection;

import roboguice.RoboGuice;

@RunWith(AndroidJUnit4.class)
public class ProtoActivityInstrumentation<T extends Activity>
        extends ActivityInstrumentationTestCase2<T> {

    protected T mActivity;
    protected JsRestClient jsRestClient;
    private Application mApplication;
    private Account activeAccount;

    public ProtoActivityInstrumentation(Class<T> activityClass) {
        super(activityClass);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());

        PreferenceApiAdapter.init(getApplication())
                .setCacheEnabled(false)
                .setInAppAnimationEnabled(false);
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        unregisterTestModule();
        mApplication = null;
        mActivity = null;
    }

    protected void setDefaultCurrentProfile() {
        AccountServerData serverData = new AccountServerData()
                .setAlias(AccountServerData.Demo.ALIAS)
                .setServerUrl(AccountServerData.Demo.SERVER_URL)
                .setOrganization(AccountServerData.Demo.ORGANIZATION)
                .setUsername(AccountServerData.Demo.USERNAME)
                .setPassword(AccountServerData.Demo.PASSWORD)
                .setEdition("PRO")
                .setVersionName("5.5");
        activeAccount = AccountUtil.get(getApplication())
                .removeAllAccounts()
                .addAccount(serverData)
                .setAuthToken()
                .activate()
                .getAccount();
        JsServerProfile profile = new JsServerProfile();
        profile.setAlias(serverData.getAlias());
        profile.setServerUrl(serverData.getServerUrl());
        profile.setOrganization(serverData.getOrganization());
        profile.setUsername(serverData.getUsername());
        profile.setPassword(serverData.getPassword());
        getJsRestClient().setServerProfile(profile);
    }

    public Account getActiveAccount() {
        if (activeAccount == null) {
            setDefaultCurrentProfile();
        }
        return activeAccount;
    }

    public void startActivityUnderTest() {
        mActivity = super.getActivity();
        // sometimes tests failed on emulator, following approach should avoid it
        // http://stackoverflow.com/questions/22737476/false-positives-junit-framework-assertionfailederror-edittext-is-not-found
        getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
                mActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        });
    }

    protected void rotate() {
        switch (mActivity.getResources().getConfiguration().orientation) {
            case Configuration.ORIENTATION_PORTRAIT:
                rotateToLandscape();
                break;
            case Configuration.ORIENTATION_LANDSCAPE:
                rotateToPortrait();
                break;
        }
    }

    protected void rotateToLandscape() {
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    protected void rotateToPortrait() {
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    protected View findViewById(int id) {
        return mActivity.findViewById(id);
    }

    protected void registerTestModule(AbstractModule module) {
        unregisterTestModule();
        RoboGuice.overrideApplicationInjector(mApplication, module);
    }

    protected void unregisterTestModule() {
        RoboGuice.Util.reset();
    }

    protected Activity getCurrentActivity() throws Throwable {
        getInstrumentation().waitForIdleSync();
        final Activity[] activity = new Activity[1];
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                Collection<Activity> activites =
                        ActivityLifecycleMonitorRegistry.getInstance()
                                .getActivitiesInStage(Stage.RESUMED);
                activity[0] = new ArrayList<Activity>(activites).get(0);
            }
        });
        return activity[0];
    }

    protected JsRestClient getJsRestClient() {
        if (jsRestClient == null) {
            Injector injector = RoboGuice.getInjector(getApplication());
            jsRestClient = injector.getInstance(JsRestClient.class);
        }
        return jsRestClient;
    }

    protected JsServerProfile getServerProfile() {
        return getJsRestClient().getServerProfile();
    }

    protected Application getApplication() {
        if (mApplication == null) {
            mApplication = (Application) this.getInstrumentation()
                    .getTargetContext().getApplicationContext();
        }
        return mApplication;
    }

    protected ContentResolver getContentResolver() {
        return getApplication().getContentResolver();
    }
}
