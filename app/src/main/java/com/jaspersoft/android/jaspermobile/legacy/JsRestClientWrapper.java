package com.jaspersoft.android.jaspermobile.legacy;

import android.content.Context;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.jaspersoft.android.jaspermobile.network.cookie.CookieStorage;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.util.KeepAliveHttpRequestInterceptor;
import com.jaspersoft.android.sdk.util.LocalesHttpRequestInterceptor;

import org.apache.http.client.CookieStore;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public class JsRestClientWrapper {
    private final DefaultPrefHelper mPrefHelper;
    private final HttpComponentsClientHttpRequestFactory mFactory;
    private final RestTemplate mRestTemplate;
    private final JsRestClient mJsRestClient;

    @Inject
    public JsRestClientWrapper(@ApplicationContext Context context, CookieStorage cookieStore) {
        mPrefHelper = DefaultPrefHelper_.getInstance_(context);
        mFactory = configureHttpFactory(cookieStore);
        mRestTemplate = configureRestTemplate();
        mJsRestClient = configureJsRestClient(mRestTemplate);
    }

    private HttpComponentsClientHttpRequestFactory configureHttpFactory(CookieStorage cookieStore) {
        CookieStore apacheCookieStore = cookieStore.getApacheCookieStore();
        CookieAwareClientHttpRequestFactory factory = new CookieAwareClientHttpRequestFactory(apacheCookieStore);
        factory.setConnectTimeout(mPrefHelper.getConnectTimeoutValue());
        factory.setReadTimeout(mPrefHelper.getReadTimeoutValue());
        return factory;
    }

    private RestTemplate configureRestTemplate() {
        RestTemplate restTemplate = new RestTemplate(false);
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new LocalesHttpRequestInterceptor());
        interceptors.add(new KeepAliveHttpRequestInterceptor());
        interceptors.add(new TokenHttpRequestInterceptor(this));
        restTemplate.setInterceptors(interceptors);
        return restTemplate;
    }

    private JsRestClient configureJsRestClient(RestTemplate restTemplate) {
        return JsRestClient.builder()
                .setDataType(JsRestClient.DataType.JSON)
                .setRestTemplate(restTemplate)
                .build();
    }

    public JsRestClient updateServerProfile(JsServerProfile serverProfile) {
        mJsRestClient.updateServerProfile(serverProfile);
        mFactory.setConnectTimeout(mPrefHelper.getConnectTimeoutValue());
        mFactory.setReadTimeout(mPrefHelper.getReadTimeoutValue());
        mRestTemplate.setRequestFactory(mFactory);
        return mJsRestClient;
    }

    public JsRestClient getClient() {
        return mJsRestClient;
    }

    private static final class CookieAwareClientHttpRequestFactory extends HttpComponentsClientHttpRequestFactory {
        private final CookieStore mCookieStore;
        private HttpContext localContext;

        private CookieAwareClientHttpRequestFactory(CookieStore cookieStore) {
            mCookieStore = cookieStore;
        }

        @Override
        protected HttpContext createHttpContext(HttpMethod httpMethod, URI uri) {
            if (localContext == null) {
                localContext = new BasicHttpContext();
                localContext.setAttribute(ClientContext.COOKIE_STORE, mCookieStore);
            }
            return localContext;
        }
    }
}
