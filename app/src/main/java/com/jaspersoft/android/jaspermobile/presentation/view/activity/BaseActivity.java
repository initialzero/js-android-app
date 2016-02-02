package com.jaspersoft.android.jaspermobile.presentation.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.ActivityModule;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Inject
    Analytics analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getAppComponent().inject(this);
        toggleScreenCapturing();
    }

    protected AppComponent getAppComponent() {
        return GraphObject.Factory.from(this).getComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }

    protected Analytics getAnalytics() {
        return analytics;
    }

    private void toggleScreenCapturing(){
        boolean isScreenCaptureEnable = DefaultPrefHelper_.getInstance_(this).isScreenCapturingEnabled();

        if (!isScreenCaptureEnable) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                    WindowManager.LayoutParams.FLAG_SECURE);
        }
    }
}
