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

package com.jaspersoft.android.jaspermobile.util.security;

import android.text.TextUtils;
import android.util.Base64;

/**
 * @author Tom Koptel
 * @since 2.1.2
 */
public final class PasswordManager {
    private static final String DELIMETER = "_";
    private final String mSalt;

    private PasswordManager(String salt) {
        mSalt = salt;
    }

    public static PasswordManager withSalt(String salt) {
        if (TextUtils.isEmpty(salt)) {
            throw new IllegalArgumentException("Salt should not be null");
        }
        return new PasswordManager(salt);
    }

    public String encrypt(String password) {
        String key = mSalt + DELIMETER + password;
        return toBase64(key.getBytes());
    }

    public String decrypt(String base64) {
        String key = new String(fromBase64(base64));
        String[] peaces = key.split(DELIMETER);
        return peaces[1];
    }

    private static String toBase64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }
}
