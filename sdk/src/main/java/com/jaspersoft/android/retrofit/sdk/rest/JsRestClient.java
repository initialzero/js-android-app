/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.retrofit.sdk.rest;

import android.content.Context;
import android.text.TextUtils;

import com.jaspersoft.android.retrofit.sdk.account.AccountDataStorage;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountDataStorage;
import com.jaspersoft.android.retrofit.sdk.ojm.ServerInfo;
import com.jaspersoft.android.retrofit.sdk.rest.response.LoginResponse;
import com.jaspersoft.android.retrofit.sdk.rest.service.AccountService;
import com.jaspersoft.android.retrofit.sdk.token.AccessTokenEncoder;
import com.jaspersoft.android.retrofit.sdk.token.BasicAccessTokenEncoder;

import java.util.List;
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
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class JsRestClient {
    private final RestAdapter mAdapter;
    private final AccountDataStorage mPreferences;
    private AccessTokenEncoder mAccessTokenEncoder;

    public static SimpleBuilder simpleBuilder(Context context) {
        return new SimpleBuilder(context);
    }

    public static BasicBuilder basicBuilder(Context context) {
        return new BasicBuilder(context);
    }

    public JsRestClient(RestAdapter adapter,
                        AccessTokenEncoder accessTokenEncoder,
                        AccountDataStorage accountPreferencesStorage) {
        mAdapter = adapter;
        mPreferences = accountPreferencesStorage;
        mAccessTokenEncoder = accessTokenEncoder;
    }

    public RestAdapter getRestAdapter() {
        return mAdapter;
    }

    public AccountDataStorage getAccountPreferences() {
        return mPreferences;
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
                .subscribeOn(Schedulers.io())
                .flatMap(new Func1<String, Observable<Response>>() {
                    @Override
                    public Observable<Response> call(String authorizationToken) {
                        return accountService.authorize(authorizationToken);
                    }
                })
                .map(new Func1<Response, List<Header>>() {
                    @Override
                    public List<Header> call(Response response) {
                        return response.getHeaders();
                    }
                })
                .flatMap(new Func1<List<Header>, Observable<Header>>() {
                    @Override
                    public Observable<Header> call(List<Header> headers) {
                        return Observable.from(headers);
                    }
                })
                .filter(new Func1<Header, Boolean>() {
                    @Override
                    public Boolean call(Header header) {
                        if (TextUtils.isEmpty(header.getName())) {
                            return false;
                        }
                        return header.getName().equals("Set-Cookie");
                    }
                })
                .collect(
                        new Func0<StringBuilder>() {
                            @Override
                            public StringBuilder call() {
                                return new StringBuilder();
                            }
                        },
                        new Action2<StringBuilder, Header>() {
                            @Override
                            public void call(StringBuilder builder, Header header) {
                                builder.append(header.getValue());
                            }
                        })
                .flatMap(new Func1<StringBuilder, Observable<LoginResponse>>() {
                    @Override
                    public Observable<LoginResponse> call(final StringBuilder header) {
                        return Observable.zip(
                                Observable.just(header.toString()),
                                accountService.getServerInfo(header.toString()),
                                new Func2<String, ServerInfo, LoginResponse>() {
                                    @Override
                                    public LoginResponse call(String header, ServerInfo serverInfo) {
                                        return new LoginResponse(header, serverInfo);
                                    }
                                });
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());
    }

    public AccountService getAccountService() {
        return mAdapter.create(AccountService.class);
    }

    public static class BasicBuilder {
        private final Context context;

        private RestAdapter restAdapter;
        private AccessTokenEncoder accessTokenEncoder;
        private AccountDataStorage accountDataStorage;

        public BasicBuilder(Context context) {
            this.context = context;
        }

        public BasicBuilder setAccessTokenEncoder(AccessTokenEncoder accessTokenEncoder) {
            this.accessTokenEncoder = accessTokenEncoder;
            return this;
        }

        public BasicBuilder setAccountDataStorage(AccountDataStorage accountDataStorage) {
            this.accountDataStorage = accountDataStorage;
            return this;
        }

        public BasicBuilder setRestAdapter(RestAdapter restAdapter) {
            this.restAdapter = restAdapter;
            return this;
        }

        public JsRestClient build() {
            ensureSaneDefaults();
            return new JsRestClient(restAdapter, accessTokenEncoder, accountDataStorage);
        }

        private void ensureSaneDefaults() {
            if (accountDataStorage == null) {
                setAccountDataStorage(BasicAccountDataStorage.get(context));
            }
        }
    }

    public static class SimpleBuilder {
        private final Context context;
        private final RestAdapter.Builder adapterBuilder;

        private AccessTokenEncoder accessTokenEncoder;
        private AccountDataStorage accountDataStorage;
        private RequestInterceptor requestInterceptor;

        public SimpleBuilder(Context context) {
            this.context = context;
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

        public SimpleBuilder setAccountDataStorage(AccountDataStorage accountDataStorage) {
            this.accountDataStorage = accountDataStorage;
            return this;
        }

        public JsRestClient build() {
            ensureSaneDefaults();
            return new JsRestClient(adapterBuilder.build(), accessTokenEncoder, accountDataStorage);
        }

        private void ensureSaneDefaults() {
            if (accountDataStorage == null) {
                setAccountDataStorage(BasicAccountDataStorage.get(context));
            }
            if (requestInterceptor == null) {
                setRequestInterceptor(new BasicRequestInterceptor());
            }
        }
    }
}
