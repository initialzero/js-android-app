package com.jaspersoft.android.jaspermobile.legacy;

import android.content.Context;

import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

/**
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
        String token = AccountManagerUtil.get(mContext).getActiveAuthToken().toBlocking().first();
        request.getHeaders().add(COOKIE, token);
        return execution.execute(request, body);
    }
}
