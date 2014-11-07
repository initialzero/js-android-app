/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.acceptance.hacked;

import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class HackedRestTemplate extends RestTemplate {
    private ClientHttpRequestFactory hackedRequestFactory;

    public HackedRestTemplate() {
        super(false);
    }

    /**
     * {@inheritDoc}
     */
    public HackedRestTemplate(boolean includeDefaultConverters) {
        super(includeDefaultConverters);
    }

    /**
     * {@inheritDoc}
     */
    public HackedRestTemplate(ClientHttpRequestFactory requestFactory) {
        super(false, requestFactory);
    }

    /**
     * Create a new instance of {@link RestTemplate} based on the given {@link ClientHttpRequestFactory}.
     * <p>For performance purposes, no message body converters are registered when using the default constructor.
     * However, this constructor allows you to specify whether to include a default set of converters, which are listed
     * in the {@link RestTemplate} javadoc.</p>
     * @param includeDefaultConverters true to add the default set of message body converters
     * @param requestFactory HTTP request factory to use
     * @see org.springframework.http.converter.HttpMessageConverter
     * @see org.springframework.http.client.SimpleClientHttpRequestFactory
     * @see org.springframework.http.client.HttpComponentsClientHttpRequestFactory
     */
    public HackedRestTemplate(boolean includeDefaultConverters, ClientHttpRequestFactory requestFactory) {
        super(includeDefaultConverters, requestFactory);
    }

    public ClientHttpRequestFactory getRequestFactory() {
        if (hackedRequestFactory == null) {
            hackedRequestFactory = new HackedHttpComponentsClientHttpRequestFactory();
        }
        return hackedRequestFactory;
    }
}
