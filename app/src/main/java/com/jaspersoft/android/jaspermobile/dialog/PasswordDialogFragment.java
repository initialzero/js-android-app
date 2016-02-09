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

package com.jaspersoft.android.jaspermobile.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.presentation.view.activity.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;
import com.jaspersoft.android.jaspermobile.util.rx.RxTransformers;
import com.jaspersoft.android.jaspermobile.util.server.InfoProvider;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class PasswordDialogFragment extends DialogFragment implements DialogInterface.OnShowListener {

    private static final String TAG = PasswordDialogFragment.class.getSimpleName();

    private EditText passwordField;
    private JasperAccountManager mJasperManager;
    private Subscription mLoginSubscription;
    private Observable<String> mLoginOperation;
    private Toast mToast;

    @Bean
    InfoProvider mInfoProvider;

    //---------------------------------------------------------------------
    // Static methods
    //---------------------------------------------------------------------

    public static void show(FragmentManager fm) {
        PasswordDialogFragment dialogFragment = (PasswordDialogFragment)
                fm.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = PasswordDialogFragment_.builder().build();
            dialogFragment.show(fm, TAG);
        }
    }

    //---------------------------------------------------------------------
    // Public methods
    //---------------------------------------------------------------------

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mToast = Toast.makeText(getActivity(), "", Toast.LENGTH_LONG);
        mJasperManager = JasperAccountManager.get(getActivity());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());

        View dialogView = inflater.inflate(R.layout.dialog_password, null);
        String alias = mInfoProvider.getAlias();
        String username = mInfoProvider.getUsername();
        String organization = mInfoProvider.getOrganization();

        ((TextView) dialogView.findViewById(R.id.tv_alias)).setText(alias);
        ((TextView) dialogView.findViewById(R.id.tv_username)).setText(username);

        TextView organizationField = (TextView) dialogView.findViewById(R.id.tv_organization);
        organizationField.setText(organization);
        if (TextUtils.isEmpty(organization)) {
            dialogView.findViewById(R.id.tv_organization_hint).setVisibility(View.GONE);
            organizationField.setVisibility(View.GONE);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.h_ad_title_server_sign_in)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.cancel, null);

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(this);

        return dialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mLoginOperation != null) {
            mLoginSubscription = subscribeToLogin(mLoginOperation);
        }
    }

    @Override
    public void onShow(DialogInterface dialogInterface) {
        AlertDialog dialog = ((AlertDialog) dialogInterface);
        passwordField = (EditText) dialog.findViewById(R.id.et_new_password);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(new PasswordDialogOkClickListener());
    }

    @Override
    public void onDestroyView() {
        if (mLoginSubscription != null) {
            mLoginSubscription.unsubscribe();
        }
        if (mToast != null) {
            mToast.cancel();
        }
        mLoginOperation = null;
        super.onDestroyView();
    }

    private void tryToLogin(String password) {
        mLoginOperation = createLoginOperation(password).cache();
        mLoginSubscription = subscribeToLogin(mLoginOperation);
    }

    private Observable<String> createLoginOperation(String password) {
        Observable<Boolean> updatePasswordOperation = mJasperManager.updateActiveAccountPassword(password);
        final Observable<String> loginOperation = mJasperManager.getActiveAuthTokenObservable();
        return updatePasswordOperation
                .concatMap(new Func1<Boolean, Observable<String>>() {
                    @Override
                    public Observable<String> call(Boolean aBoolean) {
                        return loginOperation;
                    }
                })
                .compose(RxTransformers.<String>applySchedulers());
    }

    private Subscription subscribeToLogin(Observable<String> loginOperation) {
        return loginOperation.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                loginSuccess();
            }
        }, new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                loginFailed(throwable);
            }
        });
    }

    protected void loginSuccess() {
        dismiss();
        ProgressDialogFragment.dismiss(getFragmentManager());

        int flags = Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK;
        NavigationActivity_.intent(getActivity()).flags(flags).start();
    }

    protected void loginFailed(Throwable e) {
        RequestExceptionHandler.handle(e, getActivity());
        ProgressDialogFragment.dismiss(getFragmentManager());
        mToast.setText(R.string.r_error_incorrect_credentials);
        mToast.show();
    }

    //---------------------------------------------------------------------
    // Nested classes
    //---------------------------------------------------------------------

    private class PasswordDialogOkClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            String password = passwordField.getText().toString().trim();
            if (TextUtils.isEmpty(password)) {
                passwordField.setError(getString(R.string.sp_error_field_required));
            } else {
                ProgressDialogFragment.builder(getFragmentManager())
                        .setLoadingMessage(R.string.loading_msg)
                        .show();
                tryToLogin(password);
            }
        }
    }
}