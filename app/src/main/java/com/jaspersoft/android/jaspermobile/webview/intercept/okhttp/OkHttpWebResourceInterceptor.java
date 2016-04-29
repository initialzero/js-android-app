package com.jaspersoft.android.jaspermobile.webview.intercept.okhttp;

import android.support.annotation.VisibleForTesting;
import android.webkit.WebView;

import com.jaspersoft.android.jaspermobile.webview.WebRequest;
import com.jaspersoft.android.jaspermobile.webview.intercept.WebResourceInterceptor;
import com.jaspersoft.android.jaspermobile.webview.WebResponse;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author Tom Koptel
 * @since 2.5
 */
public final class OkHttpWebResourceInterceptor implements WebResourceInterceptor {
    private final OkHttpClient client;
    private final List<WebResourceInterceptor.Rule> rules;
    private final OkRequestMapper requestMapper;
    private final OkResponseMapper responseMapper;

    public OkHttpWebResourceInterceptor() {
        this(new Builder());
    }

    private OkHttpWebResourceInterceptor(Builder builder) {
        this.client = builder.okClient;
        this.rules = builder.rules;
        this.requestMapper = builder.requestMapper;
        this.responseMapper = builder.responseMapper;
    }

    @Override
    public WebResponse interceptRequest(WebView view, WebRequest webRequest) {
        if (shouldIntercept(webRequest)) {
            try {
                return intercept(webRequest);
            } catch (IOException e) {
                return null;
            }
        }
        return null;
    }

    private WebResponse intercept(WebRequest webRequest) throws IOException {
        Request request = requestMapper.toOkHttpRequest(webRequest);
        if (request == null) {
            return null;
        } else {
            Response response = client.newCall(request).execute();
            return responseMapper.toWebViewResponse(response);
        }
    }

    public boolean shouldIntercept(WebRequest request) {
        boolean defaultResult = false;
        for (Rule rule : rules) {
            defaultResult |= rule.shouldIntercept(request);
        }
        return defaultResult;
    }

    public static class Builder {
        private OkHttpClient okClient;
        private List<WebResourceInterceptor.Rule> rules;
        private OkRequestMapper requestMapper;
        private OkResponseMapper responseMapper;

        public Builder() {
            this.rules = new ArrayList<>();
            this.requestMapper = new OkRequestMapper();
            this.responseMapper = new OkResponseMapper();
            this.okClient = new OkHttpClient();
        }

        public Builder registerRule(WebResourceInterceptor.Rule rule) {
            rules.add(rule);
            return this;
        }

        public Builder withClient(OkHttpClient client) {
            this.okClient = client;
            return this;
        }

        @VisibleForTesting
        Builder withRequestMapper(OkRequestMapper requestMapper) {
            this.requestMapper = requestMapper;
            return this;
        }

        @VisibleForTesting
        Builder withResponseMapper(OkResponseMapper responseMapper) {
            this.responseMapper = responseMapper;
            return this;
        }

        public OkHttpWebResourceInterceptor build() {
            return new OkHttpWebResourceInterceptor(this);
        }
    }
}
