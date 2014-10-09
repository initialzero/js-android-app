package com.jaspersoft.android.jaspermobile.test;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.WindowManager;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.testrunner.Stage;
import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.jaspersoft.android.jaspermobile.test.utils.DatabaseUtils;
import com.jaspersoft.android.jaspermobile.test.utils.NameUtils;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper_;
import com.squareup.spoon.Spoon;

import java.util.Collection;

import roboguice.RoboGuice;

import static com.google.common.collect.Iterables.getOnlyElement;

public class ProtoActivityInstrumentation<T extends Activity>
        extends ActivityInstrumentationTestCase2<T> {
    protected static final String USERNAME = "phoneuser|organization_1";
    protected static final String PASSWORD = "phoneuser";
    private static final long SLEEP_RATE = 0;
    protected T mActivity;
    private NameUtils nameUtils;
    private String pageName = "UNSPECIFIED";
    private ProfileHelper_ profileHelper;

    public ProtoActivityInstrumentation(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        nameUtils = new NameUtils(pageName);
        DefaultPrefHelper helper = DefaultPrefHelper_
                .getInstance_(getInstrumentation().getTargetContext().getApplicationContext());
        helper.setAnimationEnabled(false);
    }

    @Override
    protected void tearDown() throws Exception {
        nameUtils = null;
        mActivity = null;
        profileHelper = null;
        super.tearDown();
    }

    protected void setDefaultCurrentProfile() {
        Application application = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        profileHelper = ProfileHelper_.getInstance_(application);

        ContentResolver cr = application.getContentResolver();
        DatabaseUtils.deleteAllProfiles(cr);
        profileHelper.setCurrentServerProfile(DatabaseUtils.createDefaultProfile(cr));
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


    protected void makeScreenShot(String name) throws InterruptedException {
        if (SLEEP_RATE > 0) {
            Thread.sleep(SLEEP_RATE);
            Spoon.screenshot(mActivity, nameUtils.generateName(mActivity, name));
        }
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

    protected int getSearcFieldId() {
        return mActivity.getResources().getIdentifier("search_src_text", "id", "android");
    }

    protected int getActionBarId() {
        return mActivity.getResources().getIdentifier("action_bar", "id", "android");
    }

    protected int getActionBarTitleId() {
        return mActivity.getResources().getIdentifier("action_bar_title", "id", "android");
    }

    protected int getActionBarSubTitleId() {
        return mActivity.getResources().getIdentifier("action_bar_subtitle", "id", "android");
    }

    protected View findViewById(int id) {
        return mActivity.findViewById(id);
    }

    public void setPageName(String pageName) {
        this.pageName = pageName;
    }

    protected void registerTestModule(AbstractModule module) {
        unregisterTestModule();
        Application application = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        RoboGuice.setBaseApplicationInjector(application,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(application))
                        .with(module));
    }

    protected void unregisterTestModule() {
        RoboGuice.util.reset();
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
                activity[0] = getOnlyElement(activites);
            }
        });
        return activity[0];
    }
}
