/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.retrofit.sdk.token;

import android.text.TextUtils;
import android.util.Base64;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class BasicAccessTokenEncoder implements AccessTokenEncoder {
    private final String mOrganization;
    private final String mUsername;
    private final String mPassword;

    public static Builder builder() {
        return new Builder();
    }

    private BasicAccessTokenEncoder(String mOrganization, String mUsername, String mPassword) {
        this.mOrganization = mOrganization;
        this.mUsername = mUsername;
        this.mPassword = mPassword;
    }

    @Override
    public String encodeToken() {
        String mergedName = TextUtils.isEmpty(mOrganization)
                ? mUsername : (mUsername + "|" + mOrganization);
        String salt = String.format("%s:%s", mergedName, mPassword);
        return "Basic " + Base64.encodeToString(salt.getBytes(), Base64.NO_WRAP);
    }

    public static class Builder {
        private String mOrganization;
        private String mUsername;
        private String mPassword;

        public Builder setOrganization(String organization) {
            this.mOrganization = organization;
            return this;
        }

        public Builder setUsername(String username) {
            if (TextUtils.isEmpty(username)) {
                throw new IllegalArgumentException("Username should not be empty");
            }
            this.mUsername = username;
            return this;
        }

        public Builder setPassword(String password) {
            if (TextUtils.isEmpty(password)) {
                throw new IllegalArgumentException("Password should not be empty");
            }
            this.mPassword = password;
            return this;
        }

        public BasicAccessTokenEncoder build() {
            return new BasicAccessTokenEncoder(mOrganization, mUsername, mPassword);
        }
    }
}
