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

package com.jaspersoft.android.jaspermobile.test.acceptance.library.assertion;

import com.google.common.collect.Queues;
import com.jaspersoft.android.jaspermobile.test.utils.SyncSpiceManager;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.Queue;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class SpiceManagerRequestAssert extends SyncSpiceManager {
    private final Queue<RequestAssertRule> assertRules = Queues.newArrayDeque();

    public void addAssertRule(RequestAssertRule requestAssert) {
        assertRules.add(requestAssert);
    }

    @Override
    public <T> void execute(CachedSpiceRequest<T> cachedSpiceRequest, final RequestListener<T> requestListener) {
        for (RequestAssertRule rule : assertRules) {
            if (cachedSpiceRequest.getClass()
                    .isAssignableFrom(rule.getKey())) {
                assertRules.remove(rule);
                rule.getRequestAssert().assertExecution(cachedSpiceRequest, requestListener);
            }
        }
        super.execute(cachedSpiceRequest, requestListener);
    }
}