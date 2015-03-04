package com.jaspersoft.android.jaspermobile.legacy;

import android.content.Context;

import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;

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
        JasperAccountManager manager = JasperAccountManager.get(mContext);

        String token = manager.getActiveAuthToken();
        request.getHeaders().add(COOKIE, token);
        ClientHttpResponse response = execution.execute(request, body);
        HttpStatus status = response.getStatusCode();

        // Token expired
        if (status == HttpStatus.UNAUTHORIZED) {
            manager.invalidateToken(token);
            token = manager.getActiveAuthToken();
            request.getHeaders().add(COOKIE, token);
            response = execution.execute(request, body);
        }

        return response;
    }

}
