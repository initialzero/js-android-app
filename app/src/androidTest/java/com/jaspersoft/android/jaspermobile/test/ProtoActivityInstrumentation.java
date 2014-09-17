package com.jaspersoft.android.jaspermobile.test;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.test.ActivityInstrumentationTestCase2;
import android.view.View;
import android.view.WindowManager;
import android.widget.ListView;

import com.google.inject.AbstractModule;
import com.google.inject.util.Modules;
import com.jaspersoft.android.jaspermobile.test.utils.NameUtils;
import com.squareup.spoon.Spoon;

import roboguice.RoboGuice;

public class ProtoActivityInstrumentation<T extends Activity>
        extends ActivityInstrumentationTestCase2<T> {
    protected static final String USERNAME = "phoneuser|organization_1";
    protected static final String PASSWORD = "phoneuser";
    private static final long SLEEP_RATE = 0;
    protected T mActivity;
    private NameUtils nameUtils;
    private String pageName = "UNSPECIFIED";

    public ProtoActivityInstrumentation(Class<T> activityClass) {
        super(activityClass);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        nameUtils = new NameUtils(pageName);
    }

    @Override
    protected void tearDown() throws Exception {
        nameUtils = null;
        mActivity = null;
        super.tearDown();
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
        switch(mActivity.getResources().getConfiguration().orientation) {
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

    protected void makeTwoFirstListItemsAccessible() {
        ListView list = (ListView) mActivity.findViewById(android.R.id.list);
        View firstItem = list.getChildAt(0);
        firstItem.setId(com.jaspersoft.android.jaspermobile.test.R.id.firs_list_item);
        View secondItem = list.getChildAt(1);
        secondItem.setId(com.jaspersoft.android.jaspermobile.test.R.id.second_list_item);
    }

    protected void registerTestModule(AbstractModule module) {
        Application application = (Application) this.getInstrumentation()
                .getTargetContext().getApplicationContext();
        RoboGuice.setBaseApplicationInjector(application,
                RoboGuice.DEFAULT_STAGE,
                Modules.override(RoboGuice.newDefaultRoboModule(application))
                        .with(module));
    }
}
