package com.jaspersoft.android.jaspermobile.legacy;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountProvider;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
 * For description of flow refer to http://code2flow.com/uyFdCJ
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class TokenHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final String COOKIE = "Cookie";

    private final Context mContext;

    public TokenHttpRequestInterceptor(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String token = AccountManagerUtil.get(mContext)
                .getActiveAuthToken()
                .toBlocking()
                .firstOrDefault(null);
        request.getHeaders().add(COOKIE, token);

        ClientHttpResponse response = execution.execute(request, body);

        HttpStatus status = response.getStatusCode();

        if (status == HttpStatus.UNAUTHORIZED) {
            Intent intent = new Intent();

            if (TextUtils.isEmpty(token)) {
                Account account = JasperAccountProvider.get(mContext).getAccount();
                intent.setAction(JasperSettings.ACTION_INVALID_PASSWORD);
                intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
            } else {
                intent.setAction(JasperSettings.ACTION_TOKEN_EXPIRED);
            }

            mContext.sendBroadcast(intent);
        }

        return response;
    }

}
