/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.retrofit.sdk.rest;

import android.text.TextUtils;

import com.jaspersoft.android.retrofit.sdk.ojm.ServerInfo;
import com.jaspersoft.android.retrofit.sdk.rest.response.LoginResponse;
import com.jaspersoft.android.retrofit.sdk.rest.service.AccountService;
import com.jaspersoft.android.retrofit.sdk.token.AccessTokenEncoder;
import com.jaspersoft.android.retrofit.sdk.token.BasicAccessTokenEncoder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executor;

import retrofit.Endpoint;
import retrofit.ErrorHandler;
import retrofit.Profiler;
import retrofit.RequestInterceptor;
import retrofit.RestAdapter;
import retrofit.client.Client;
import retrofit.client.Header;
import retrofit.client.Response;
import retrofit.converter.Converter;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.functions.Func2;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class JsRestClient2 {
    private final RestAdapter mAdapter;
    private AccessTokenEncoder mAccessTokenEncoder;

    public static SimpleBuilder configure() {
        return new SimpleBuilder();
    }

    public static BasicBuilder builder() {
        return new BasicBuilder();
    }

    public static JsRestClient2 forEndpoint(Endpoint enpoint) {
        return configure().setEndpoint(enpoint).build();
    }

    public static JsRestClient2 forEndpoint(String enpoint) {
        return configure().setEndpoint(enpoint).build();
    }

    public JsRestClient2(RestAdapter adapter,
                         AccessTokenEncoder accessTokenEncoder) {
        mAdapter = adapter;
        mAccessTokenEncoder = accessTokenEncoder;
    }

    public RestAdapter getRestAdapter() {
        return mAdapter;
    }


    public void setAccessTokenEncoder(AccessTokenEncoder accessTokenEncoder) {
        mAccessTokenEncoder = accessTokenEncoder;
    }

    public Observable<LoginResponse> login() {
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                if (mAccessTokenEncoder == null) {
                    subscriber.onError(new RuntimeException("Set AccessTokenEncoder before method invocation"));
                }
                try {
                    subscriber.onNext(mAccessTokenEncoder.encodeToken());
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });

        return login(observable);
    }

    public Observable<LoginResponse> login(final String organization, final String username, final String password) {
        Observable<String> observable = Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    BasicAccessTokenEncoder encoder = BasicAccessTokenEncoder.builder()
                            .setOrganization(organization)
                            .setUsername(username)
                            .setPassword(password)
                            .build();
                    subscriber.onNext(encoder.encodeToken());
                    subscriber.onCompleted();
                } catch (Exception ex) {
                    subscriber.onError(ex);
                }
            }
        });

        return login(observable);
    }

    public Observable<LoginResponse> login(Observable<String> tokenObservable) {
        final AccountService accountService = getAccountService();
        return tokenObservable
                .flatMap(new Func1<String, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(String authorizationToken) {
                        String locale = Locale.getDefault().toString().replaceAll("_", "-");
                        return accountService.authorize(authorizationToken, locale);
                    }
                })
                .map(new Func1<Response, List<Header>>() {
                    @Override
                    public List<Header> call(Response response) {
                        return response.getHeaders();
                    }
                })
                .flatMap(new Func1<List<Header>, Observable<String>>() {
                    @Override
                    public Observable<String> call(List<Header> headers) {
                        List<String> cookies = new ArrayList<String>();
                        for (Header header : headers) {
                            if (!TextUtils.isEmpty(header.getName()) && header.getName().equals("Set-Cookie")) {
                                cookies.add(header.getValue());
                            }
                        }

                        StringBuilder stringBuilder = new StringBuilder();
                        Iterator<String> iterator = cookies.iterator();
                        while (iterator.hasNext()) {
                            String cookie = iterator.next();
                            stringBuilder.append(cookie);
                            if (iterator.hasNext()) {
                                stringBuilder.append(";");
                            }
                        }

                        return Observable.just(stringBuilder.toString());
                    }
                })
                .flatMap(new Func1<String, Observable<LoginResponse>>() {
                    @Override
                    public Observable<LoginResponse> call(final String header) {
                        return Observable.zip(
                                Observable.just(header),
                                accountService.getServerInfo(header),
                                new Func2<String, ServerInfo, LoginResponse>() {
                                    @Override
                                    public LoginResponse call(String header, ServerInfo serverInfo) {
                                        return new LoginResponse(header, serverInfo);
                                    }
                                });
                    }
                });
    }

    public AccountService getAccountService() {
        return mAdapter.create(AccountService.class);
    }

    public static class BasicBuilder {
        private RestAdapter restAdapter;
        private AccessTokenEncoder accessTokenEncoder;

        public BasicBuilder() {
        }

        public BasicBuilder setAccessTokenEncoder(AccessTokenEncoder accessTokenEncoder) {
            this.accessTokenEncoder = accessTokenEncoder;
            return this;
        }

        public BasicBuilder setRestAdapter(RestAdapter restAdapter) {
            this.restAdapter = restAdapter;
            return this;
        }

        public JsRestClient2 build() {
            return new JsRestClient2(restAdapter, accessTokenEncoder);
        }
    }

    public static class SimpleBuilder {
        private final RestAdapter.Builder adapterBuilder;

        private AccessTokenEncoder accessTokenEncoder;
        private RequestInterceptor requestInterceptor;

        public SimpleBuilder() {
            this.adapterBuilder = new RestAdapter.Builder();
        }

        public SimpleBuilder setEndpoint(String endpoint) {
            adapterBuilder.setEndpoint(endpoint);
            return this;
        }

        public SimpleBuilder setEndpoint(Endpoint endpoint) {
            adapterBuilder.setEndpoint(endpoint);
            return this;
        }

        public SimpleBuilder setClient(final Client client) {
            adapterBuilder.setClient(client);
            return this;
        }

        public SimpleBuilder setClient(Client.Provider clientProvider) {
            adapterBuilder.setClient(clientProvider);
            return this;
        }

        public SimpleBuilder setExecutors(Executor httpExecutor, Executor callbackExecutor) {
            adapterBuilder.setExecutors(httpExecutor, callbackExecutor);
            return this;
        }

        public SimpleBuilder setRequestInterceptor(RequestInterceptor requestInterceptor) {
            adapterBuilder.setRequestInterceptor(requestInterceptor);
            this.requestInterceptor = requestInterceptor;
            return this;
        }

        public SimpleBuilder setConverter(Converter converter) {
            adapterBuilder.setConverter(converter);
            return this;
        }

        public SimpleBuilder setProfiler(Profiler profiler) {
            adapterBuilder.setProfiler(profiler);
            return this;
        }

        public SimpleBuilder setErrorHandler(ErrorHandler errorHandler) {
            adapterBuilder.setErrorHandler(errorHandler);
            return this;
        }

        public SimpleBuilder setLog(RestAdapter.Log log) {
            adapterBuilder.setLog(log);
            return this;
        }

        public SimpleBuilder setLogLevel(RestAdapter.LogLevel logLevel) {
            adapterBuilder.setLogLevel(logLevel);
            return this;
        }

        public SimpleBuilder setAccessTokenEncoder(AccessTokenEncoder accessTokenEncoder) {
            this.accessTokenEncoder = accessTokenEncoder;
            return this;
        }

        public JsRestClient2 build() {
            ensureSaneDefaults();
            return new JsRestClient2(adapterBuilder.build(), accessTokenEncoder);
        }

        private void ensureSaneDefaults() {
            if (requestInterceptor == null) {
                setRequestInterceptor(new BasicRequestInterceptor());
            }
        }
    }
}
