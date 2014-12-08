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

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class RequestAssertRule {
    private final Class<?> mClass;
    private final RequestAssert requestAssert;

    public static Builder builder() {
        return new Builder();
    }

    private RequestAssertRule(Class<?> mClass, RequestAssert requestAssert) {
        this.mClass = mClass;
        this.requestAssert = requestAssert;
    }

    public Class<?> getKey() {
        return mClass;
    }

    public RequestAssert getRequestAssert() {
        return requestAssert;
    }

    public static class Builder {
        private Class<?> mClass;
        private RequestAssert requestAssert;

        public Builder setRequestKey(Class<?> mClass) {
            this.mClass = mClass;
            return this;
        }

        public Builder setRequestAssert(RequestAssert requestAssert) {
            this.requestAssert = requestAssert;
            return this;
        }

        public RequestAssertRule create() {
            return new RequestAssertRule(mClass, requestAssert);
        }
    }
}