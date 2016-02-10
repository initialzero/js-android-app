package com.jaspersoft.android.jaspermobile.presentation.view.activity;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.modules.activity.StartupActivityModule;
import com.jaspersoft.android.jaspermobile.presentation.contract.StartupContract;
import com.jaspersoft.android.jaspermobile.presentation.presenter.StartupPresenter;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class StartupActivity extends Activity {

    @Inject
    @Named("SIGN_UP_REQUEST")
    Integer signUpRequestCode;

    @Inject
    StartupContract.ActionListener mActionListener;

    @Inject
    StartupPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        GraphObject.Factory.from(this)
                .getComponent()
                .plus(new StartupActivityModule(this))
                .inject(this);

        mActionListener.tryToSetupProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == signUpRequestCode) {
            onSignUp(resultCode, data);
        }
    }

    private void onSignUp(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            String profileName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Profile profile = Profile.create(profileName);
            mActionListener.setupNewProfile(profile);
        } else {
            finish();
        }
    }
}
