package com.jaspersoft.android.jaspermobile.presentation.view.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.modules.ActivityModule;
import com.jaspersoft.android.jaspermobile.util.ActivitySecureDelegate;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public abstract class BaseActivity extends AppCompatActivity {

    private ActivitySecureDelegate mActivitySecureDelegate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mActivitySecureDelegate = ActivitySecureDelegate.create(this);
        super.onCreate(savedInstanceState);
        mActivitySecureDelegate.onCreate(savedInstanceState);

        getAppComponent().inject(this);
    }

    protected AppComponent getAppComponent() {
        return ((JasperMobileApplication)getApplication()).getComponent();
    }

    protected ActivityModule getActivityModule() {
        return new ActivityModule(this);
    }
}
