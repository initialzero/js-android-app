package com.jaspersoft.android.jaspermobile.legacy;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountProvider;
import com.jaspersoft.android.retrofit.sdk.token.BasicAccessTokenEncoder;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.HttpCookie;
import java.util.List;

/**
 * For description of flow refer to http://code2flow.com/uyFdCJ
 *
 * @author Tom Koptel
 * @since 2.0
 */
public class TokenHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    private static final String COOKIE = "Cookie";
    private static final String AUTHORIZE = "Authorization";

    private final Context mContext;

    public TokenHttpRequestInterceptor(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        Account account = JasperAccountProvider.get(mContext).getAccount();
        AccountManager accountManager = AccountManager.get(mContext);

        String token = AccountManagerUtil.get(mContext)
                .getActiveAuthToken()
                .toBlocking()
                .firstOrDefault(null);
        if (TextUtils.isEmpty(token)) {
            request.getHeaders().add(AUTHORIZE, createAuthorizationToken(accountManager, account));
        } else {
            List<HttpCookie> cookies = HttpCookie.parse(token);
            HttpCookie cookie = cookies.get(0);
            if (cookie.hasExpired()) {
                Intent intent = new Intent(JasperSettings.ACTION_TOKEN_EXPIRED);
                mContext.sendBroadcast(intent);

                accountManager.invalidateAuthToken(JasperSettings.JASPER_ACCOUNT_TYPE, token);
                request.getHeaders().add(AUTHORIZE, createAuthorizationToken(accountManager, account));
            } else {
                request.getHeaders().add(COOKIE, token);
            }
        }

        ClientHttpResponse response = execution.execute(request, body);

        HttpStatus status = response.getStatusCode();
        if (status == HttpStatus.UNAUTHORIZED) {
            if (TextUtils.isEmpty(token)) {
                Intent intent = new Intent(JasperSettings.ACTION_INVALID_PASSWORD);
                intent.putExtra(AccountManager.KEY_ACCOUNT_NAME, account.name);
                mContext.sendBroadcast(intent);
            } else {
                accountManager.invalidateAuthToken(JasperSettings.JASPER_ACCOUNT_TYPE, token);
            }
        }

        return response;
    }

    private String createAuthorizationToken(AccountManager accountManager, Account account) {
        String password = accountManager.getPassword(account);
        AccountServerData serverData = AccountServerData.get(mContext, account);
        return BasicAccessTokenEncoder.builder()
                .setOrganization(serverData.getOrganization())
                .setUsername(serverData.getUsername())
                .setPassword(password)
                .build()
                .encodeToken();
    }
}
