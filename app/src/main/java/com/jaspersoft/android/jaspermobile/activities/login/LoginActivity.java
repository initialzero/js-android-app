package com.jaspersoft.android.jaspermobile.activities.login;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.profile.ServersManagerActivity_;
import com.jaspersoft.android.jaspermobile.activities.profile.fragment.ServersFragment;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceFragmentActivity;
import com.jaspersoft.android.jaspermobile.dialog.AlertDialogFragment;
import com.jaspersoft.android.sdk.client.JsServerProfile;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
@EActivity(R.layout.login_layout)
public class LoginActivity extends RoboSpiceFragmentActivity {

    public final static String ACCOUNT_TYPE = "com.jaspersoft.android.jaspermobile.account";
    public final static String AUTH_TOKEN_TYPE = "com.jaspersoft.android.jaspermobile.cookie_token";
    public final static String ACCOUNT_NAME = "js_account_name";
    public static final int RC_SWITCH_SERVER_PROFILE = 21;

    // TEMPORARY
    private AccountAuthenticatorResponse mAccountAuthenticatorResponse = null;
    private Bundle mResultBundle = null;

    @InstanceState
    JsServerProfile serverProfile;

    @ViewById(R.id.et_login)
    EditText etLogin;

    @ViewById(R.id.et_password)
    EditText etPassword;

    @ViewById(R.id.tv_select_server)
    TextView tvServer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccountAuthenticatorResponse =
                getIntent().getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE);

        if (mAccountAuthenticatorResponse != null) {
            mAccountAuthenticatorResponse.onRequestContinued();
        }
    }

    @Click(R.id.tv_select_server)
    final void selectServer() {
        ServersManagerActivity_.intent(this)
                .serverId(serverProfile != null ? serverProfile.getId() : -1)
                .startForResult(RC_SWITCH_SERVER_PROFILE);
    }

    @Click(R.id.btn_login)
    final void login() {
        final String userName = etLogin.getText().toString();
        final String userPass = etPassword.getText().toString();

        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userPass) && serverProfile != null) {
            JasperMobileApplication.removeAllCookies();

            // ServerLogin instance
            // serverLogin.userSignIn
            // serverLogic.setCallback

        }
    }

    @OnActivityResult(RC_SWITCH_SERVER_PROFILE)
    final void switchServerProfile(int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Bundle extras = data.getExtras();
            serverProfile = extras.getParcelable(ServersFragment.EXTRA_SERVER_PROFILE);

            String profileName = serverProfile.getAlias();
            tvServer.setText(profileName);
        }
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    //TEMPORARY
    public final void setAccountAuthenticatorResult(Bundle result) {
        mResultBundle = result;
    }

    public void finish() {
        if (mAccountAuthenticatorResponse != null) {
            // send the result bundle back if set, otherwise send an error.
            if (mResultBundle != null) {
                mAccountAuthenticatorResponse.onResult(mResultBundle);
            } else {
                mAccountAuthenticatorResponse.onError(AccountManager.ERROR_CODE_CANCELED,
                        "canceled");
            }
            mAccountAuthenticatorResponse = null;
        }
        super.finish();
    }

    private ServerLogin.LoginCallback listener = new ServerLogin.LoginCallback() {
        @Override
        public void onInvalidServerCredential() {
            Toast.makeText(LoginActivity.this, "Invalid server credentials", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onInvalidServerVersion() {
            AlertDialogFragment.createBuilder(LoginActivity.this, getSupportFragmentManager())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setTitle(R.string.error_msg)
                    .setMessage(R.string.r_error_server_not_supported)
                    .show();
        }

        @Override
        public void onInvalidUserCredential() {
            Toast.makeText(LoginActivity.this, "Invalid login or password", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLogin() {
            final String userName = etLogin.getText().toString();
            final String userPass = etPassword.getText().toString();

            final Account account = new Account(userName, ACCOUNT_TYPE);
            AccountManager mAccountManager = AccountManager.get(LoginActivity.this);
            mAccountManager.addAccountExplicitly(account, userPass, null);
            mAccountManager.setAuthToken(account, AUTH_TOKEN_TYPE, "authtoken");
            mAccountManager.setPassword(account, userPass);
            Intent resultIntent = new Intent();
            Bundle data = new Bundle();
            data.putString(AccountManager.KEY_ACCOUNT_NAME, userName);
            data.putString(AccountManager.KEY_ACCOUNT_TYPE, ACCOUNT_TYPE);
            data.putString(AccountManager.KEY_AUTHTOKEN, "authtoken");
            data.putString(AccountManager.KEY_PASSWORD, userPass);
            resultIntent.putExtras(data);
            setAccountAuthenticatorResult(resultIntent.getExtras());
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    };

}
