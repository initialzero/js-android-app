package com.jaspersoft.android.jaspermobile.presentation.navigation;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerActivity
public class Navigator {
    private final Activity mActivity;

    @Inject
    public Navigator(Activity activity) {
        mActivity = activity;
    }

    public void navigate(Page page, boolean finishCurrent) {
        start(page, finishCurrent);
    }

    public void navigateForResult(Page page, int result, boolean finishCurrent) {
        startForResult(page, result, finishCurrent);
    }

    private void start(Page page, boolean finishCurrent) {
        mActivity.startActivity(page.getIntent());
        if (finishCurrent) {
            mActivity.finish();
        }
    }

    private void startForResult(Page page, int result, boolean finishCurrent) {
        mActivity.startActivityForResult(page.getIntent(), result);
        if (finishCurrent) {
            mActivity.finish();
        }
    }

    @NonNull
    public static Navigator from(@NonNull Activity activity) {
        return new Navigator(activity);
    }
}
