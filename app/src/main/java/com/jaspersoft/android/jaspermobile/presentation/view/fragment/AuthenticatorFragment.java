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

import android.accounts.Account;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.EditText;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.domain.BaseCredentials;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.internal.di.components.SaveProfileComponent;
import com.jaspersoft.android.jaspermobile.presentation.presenter.AuthenticationPresenter;
import com.jaspersoft.android.jaspermobile.util.JasperSettings;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EFragment(R.layout.add_account_layout)
public class AuthenticatorFragment extends BaseFragment {
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

    @SystemService
    protected InputMethodManager inputMethodManager;

    @Inject
    AuthenticationPresenter mPresenter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getComponent(SaveProfileComponent.class).inject(this);
    }

    @Click
    void addAccount() {
        if (isFormValid()) {
            String alias = aliasEdit.getText().toString();
            String serverUrl = serverUrlEdit.getText().toString();
            String username = usernameEdit.getText().toString();
            String password = passwordEdit.getText().toString();
            String organization = organizationEdit.getText().toString();

            Profile profile = Profile.create(alias);
            BaseCredentials credentials = BaseCredentials.builder()
                    .setUsername(username)
                    .setPassword(password)
                    .setOrganization(organization)
                    .create();
        }
    }

    private boolean isFormValid() {
        String serverUrl = serverUrlEdit.getText().toString();
        String alias = aliasEdit.getText().toString();

        Map<EditText, String> valueMap = new HashMap<EditText, String>();
        valueMap.put(aliasEdit, alias);
        valueMap.put(serverUrlEdit, serverUrl);
        valueMap.put(usernameEdit, usernameEdit.getText().toString());
        valueMap.put(passwordEdit, passwordEdit.getText().toString());

        boolean isFieldValid;
        boolean formValid = true;
        for (Map.Entry<EditText, String> entry : valueMap.entrySet()) {
            isFieldValid = !TextUtils.isEmpty(entry.getValue().trim());
            if (!isFieldValid) {
                entry.getKey().setError(getString(R.string.sp_error_field_required));
                entry.getKey().requestFocus();
            }
            formValid &= isFieldValid;
        }

        if (!TextUtils.isEmpty(serverUrl)) {
            String url = trimUrl(serverUrl);
            if (!URLUtil.isNetworkUrl(url)) {
                serverUrlEdit.setError(getString(R.string.sp_error_url_not_valid));
                serverUrlEdit.requestFocus();
                formValid &= false;
            }
        }

        if (!TextUtils.isEmpty(alias)) {
            Account account = new Account(alias, JasperSettings.JASPER_ACCOUNT_TYPE);
            List<Account> accountList = new ArrayList<Account>();
            Collections.addAll(accountList, JasperAccountManager.get(getActivity()).getAccounts());
            if (accountList.contains(account)) {
                aliasEdit.setError(getString(R.string.sp_error_duplicate_alias));
                aliasEdit.requestFocus();
                formValid &= false;
            }

            if (alias.equals(JasperSettings.RESERVED_ACCOUNT_NAME)) {
                aliasEdit.setError(getString(R.string.sp_error_reserved_alias));
                aliasEdit.requestFocus();
                formValid &= false;
            }
        }

        return formValid;
    }

    private String trimUrl(String url) {
        if (!TextUtils.isEmpty(url) && url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }

    private void hideKeyboard() {
        View focus = getActivity().getCurrentFocus();
        if (focus != null) {
            IBinder token = focus.getWindowToken();
            if (token != null) {
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            }
        }
    }
}
