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

package com.jaspersoft.android.jaspermobile.presentation.view.fragment;

import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.EditText;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.internal.di.components.SaveProfileComponent;
import com.jaspersoft.android.jaspermobile.presentation.action.ProfileActionListener;
import com.jaspersoft.android.jaspermobile.presentation.model.CredentialsModel;
import com.jaspersoft.android.jaspermobile.presentation.model.ProfileModel;
import com.jaspersoft.android.jaspermobile.presentation.presenter.AuthenticationPresenter;
import com.jaspersoft.android.jaspermobile.presentation.view.AuthenticationView;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EFragment(R.layout.add_account_layout)
public class AuthenticatorFragment extends BaseFragment implements AuthenticationView {
    @ViewById
    protected EditText aliasEdit;
    @ViewById
    protected EditText usernameEdit;
    @ViewById
    protected EditText organizationEdit;
    @ViewById
    protected EditText serverUrlEdit;
    @ViewById
    protected EditText passwordEdit;
    @ViewById(R.id.tryDemoContainer)
    protected ViewGroup tryDemoLayout;

    @Inject
    AuthenticationPresenter mPresenter;
    @Inject
    ProfileActionListener mProfileActionListener;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        injectComponents();
    }

    private void injectComponents() {
        getComponent(SaveProfileComponent.class).inject(this);
        mPresenter.setView(this);
    }

    @Click
    void addAccount() {
        String alias = aliasEdit.getText().toString();
        String serverUrl = serverUrlEdit.getText().toString();
        String username = usernameEdit.getText().toString();
        String password = passwordEdit.getText().toString();
        String organization = organizationEdit.getText().toString();

        CredentialsModel credentials = CredentialsModel.builder()
                .setUsername(username)
                .setPassword(password)
                .setOrganization(organization)
                .create();
        ProfileModel profile = ProfileModel.builder()
                .setAlias(alias)
                .setBaseUrl(serverUrl)
                .setCredentials(credentials)
                .create();
        mProfileActionListener.saveProfile(profile);
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.pause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mPresenter.destroy();
    }

    @Override
    public void showLoading() {
        // no op
    }

    @Override
    public void hideLoading() {
        // no op
    }

    @Override
    public void showRetry() {
        // no op
    }

    @Override
    public void hideRetry() {
        // no op
    }

    @Override
    public void showError(String message) {
        // no op
    }

    @Override
    public void showAliasDuplicateError() {
        // no op
    }

    @Override
    public void showAliasReservedError() {
        // no op
    }

    @Override
    public void showAliasRequiredError() {
        // no op
    }

    @Override
    public void showServerUrlFormatError() {
        // no op
    }

    @Override
    public void showServerUrlRequiredError() {
        // no op
    }

    @Override
    public void showUsernameRequiredError() {
        // no op
    }
}
