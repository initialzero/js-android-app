/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.data.repository.report;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.data.cache.profile.JasperServerCache;
import com.jaspersoft.android.jaspermobile.data.cache.report.VisualizeTemplateCache;
import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.repository.report.VisualizeTemplateRepository;
import com.jaspersoft.android.jaspermobile.internal.di.ApplicationContext;
import com.jaspersoft.android.jaspermobile.util.VisualizeEndpoint;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;
import com.samskivert.mustache.Mustache;
import com.samskivert.mustache.Template;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func0;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Singleton
public final class InMemoryVisualizeTemplateRepository implements VisualizeTemplateRepository {
    private static final int RETRY_COUNT = 5;

    private final Context mContext;
    private final VisualizeTemplateCache mCache;
    private final JasperServerCache mJasperServerCache;

    @Inject
    public InMemoryVisualizeTemplateRepository(@ApplicationContext Context context,
                                               VisualizeTemplateCache cache,
                                               JasperServerCache jasperServerCache) {
        mContext = context;
        mCache = cache;
        mJasperServerCache = jasperServerCache;
    }

    @Override
    public Observable<VisualizeTemplate> get(final Profile profile, final Map<String, ?> clientParams) {
        Observable<VisualizeTemplate> diskSource = Observable.defer(new Func0<Observable<VisualizeTemplate>>() {
            @Override
            public Observable<VisualizeTemplate> call() {
                JasperServer server = mJasperServerCache.get(profile);
                String versionName = server.getVersion();
                if (versionName == null) {
                    throw new IllegalStateException("Server version missing impossible to load server");
                }
                ServerVersion version = ServerVersion.valueOf(versionName);
                boolean optimized = !(version.greaterThanOrEquals(ServerVersion.v6) && version.lessThan(ServerVersion.v6_1));

                InputStream stream = null;
                try {
                    stream = mContext.getAssets().open("report.html");
                    StringWriter writer = new StringWriter();
                    IOUtils.copy(stream, writer, "UTF-8");

                    String baseUrl = server.getBaseUrl();
                    VisualizeEndpoint visualizeEndpoint = VisualizeEndpoint.forBaseUrl(baseUrl)
                            .setOptimized(optimized)
                            .build();
                    String visualizeUrl = visualizeEndpoint.createUri();

                    Map<String, Object> data = new HashMap<>();
                    data.put("visualize_url", visualizeUrl);
                    data.put("optimized", optimized);
                    data.putAll(clientParams);

                    Template tmpl = Mustache.compiler().compile(writer.toString());
                    String html = tmpl.execute(data);

                    return Observable.just(new VisualizeTemplate(html, baseUrl));
                } catch (IOException e) {
                    return Observable.error(e);
                } finally {
                    if (stream != null) {
                        IOUtils.closeQuietly(stream);
                    }
                }
            }
        })
                .retry(RETRY_COUNT)
                .doOnNext(new Action1<VisualizeTemplate>() {
                    @Override
                    public void call(VisualizeTemplate template) {
                        mCache.put(profile, template);
                    }
                });

        return diskSource;
    }
}
