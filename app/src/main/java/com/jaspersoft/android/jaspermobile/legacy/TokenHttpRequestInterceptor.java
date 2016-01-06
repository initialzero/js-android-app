package com.jaspersoft.android.jaspermobile.legacy;

import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.network.AnonymousClient;
import com.jaspersoft.android.sdk.network.Credentials;
import com.jaspersoft.android.sdk.network.HttpException;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.network.SpringCredentials;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.net.CookieManager;

/**
 * @author Tom Koptel
 * @since 2.3
 */
final class TokenHttpRequestInterceptor implements ClientHttpRequestInterceptor {
    private final JsRestClientWrapper mJsRestClientWrapper;

    public TokenHttpRequestInterceptor(JsRestClientWrapper jsRestClientWrapper) {
        mJsRestClientWrapper = jsRestClientWrapper;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request,
                                        byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        ClientHttpResponse firstResponse = execution.execute(request, body);
        HttpStatus firstStatus = firstResponse.getStatusCode();
        if (firstStatus == HttpStatus.UNAUTHORIZED) {
            JsRestClient jsRestClient = mJsRestClientWrapper.getClient();
            JsServerProfile profile = jsRestClient.getServerProfile();
            Server server = Server.builder()
                    .withBaseUrl(profile.getServerUrl() + "/")
                    .build();
            AnonymousClient client = server.newClient()
                    .withCookieHandler(CookieManager.getDefault())
                    .create();

            Credentials credentials = SpringCredentials.builder()
                    .withPassword(profile.getPassword())
                    .withUsername(profile.getUsername())
                    .withOrganization(profile.getOrganization())
                    .build();
            try {
                client.authenticationApi().authenticate(credentials);
            } catch (HttpException e) {
                return firstResponse;
            }

            ClientHttpResponse secondResponse = execution.execute(request, body);
            HttpStatus secondStatus = firstResponse.getStatusCode();
            if (secondStatus == HttpStatus.UNAUTHORIZED) {
                return secondResponse;
            }
        }
        return firstResponse;
    }
}
