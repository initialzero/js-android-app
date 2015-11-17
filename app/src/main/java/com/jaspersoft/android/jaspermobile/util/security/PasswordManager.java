/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util.security;

import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Base64;

import java.io.UnsupportedEncodingException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/**
 * @author Tom Koptel
 * @since 2.1.2
 */
public final class PasswordManager {
    protected static final String UTF8 = "utf-8";
    private static int ITERATION_COUNT = 1000;

    private final char[] mSecret;
    private final Context mContext;

    private PasswordManager(Context context, String secret) {
        mContext = context;
        mSecret = secret.toCharArray();
    }

    public static PasswordManager init(Context context, String secret) {
        if (TextUtils.isEmpty(secret)) {
            throw new IllegalArgumentException("Secret should not be null or empty");
        }
        return new PasswordManager(context, secret);
    }

    public String encrypt(String value) {
        try {
            final byte[] bytes = value != null ? value.getBytes(UTF8) : new byte[0];

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            KeySpec keySpec = new PBEKeySpec(mSecret);
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            AlgorithmParameterSpec spec = new PBEParameterSpec(fetchSalt(), ITERATION_COUNT);

            pbeCipher.init(Cipher.ENCRYPT_MODE, key, spec);

            return toBase64(pbeCipher.doFinal(bytes));
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String decrypt(String value) {
        try {
            final byte[] bytes = value != null ? fromBase64(value) : new byte[0];

            SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBEWithMD5AndDES");
            KeySpec keySpec = new PBEKeySpec(mSecret);
            SecretKey key = keyFactory.generateSecret(keySpec);

            Cipher pbeCipher = Cipher.getInstance("PBEWithMD5AndDES");
            AlgorithmParameterSpec spec = new PBEParameterSpec(fetchSalt(), ITERATION_COUNT);

            pbeCipher.init(Cipher.DECRYPT_MODE, key, spec);

            return new String(pbeCipher.doFinal(bytes), UTF8);
        } catch (Exception ex) {
            return null;
        }
    }

    private static String toBase64(byte[] bytes) throws UnsupportedEncodingException {
        return new String(Base64.encode(bytes, Base64.NO_WRAP), UTF8);
    }

    private static byte[] fromBase64(String base64) {
        return Base64.decode(base64, Base64.DEFAULT);
    }

    private byte[] fetchSalt() throws UnsupportedEncodingException {
        String id = Settings.Secure.getString(mContext.getContentResolver(),
                Settings.Secure.ANDROID_ID);

        return Arrays.copyOf(id.getBytes(UTF8), 8);
    }
}
