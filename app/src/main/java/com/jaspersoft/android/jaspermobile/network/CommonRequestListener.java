/*
 * Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
 * http://community.jaspersoft.com/project/mobile-sdk-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile SDK for Android.
 *
 * Jaspersoft Mobile SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile SDK is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile SDK for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.network;

import android.app.Activity;

import com.octo.android.robospice.persistence.exception.SpiceException;

import org.springframework.http.HttpStatus;

import java.util.EnumMap;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@Deprecated
public abstract class CommonRequestListener<T> extends SimpleRequestListener<T> {

    private final EnumMap<HttpStatus, ExceptionRule> rules;

    protected CommonRequestListener() {
        this.rules = ExceptionRule.all();
    }

    public CommonRequestListener(EnumMap<HttpStatus, ExceptionRule> rules) {
        this.rules = rules;
    }

    public void addRule(ExceptionRule rule) {
        rules.put(rule.getHttpStatus(), rule);
    }

    public void removeRule(ExceptionRule rule) {
        rules.remove(rule.getHttpStatus());
    }

    protected HttpStatus extractStatusCode(SpiceException spiceException) {
        return RequestExceptionHandler.extractStatusCode(spiceException);
    }

    @Override
    public final void onRequestFailure(SpiceException spiceException) {
        RequestExceptionHandler.handle(spiceException, getCurrentActivity(), rules);
        onSemanticFailure(spiceException);
    }

    @Override
    public final void onRequestSuccess(T data) {
        onSemanticSuccess(data);
    }

    public void onSemanticFailure(SpiceException spiceException) {
        // Override method for additional implementation
    }

    public void onSemanticSuccess(T data) {
        // Override method for additional implementation
    }

    public EnumMap<HttpStatus, ExceptionRule> getRules() {
        return rules;
    }

    public abstract Activity getCurrentActivity();

}
