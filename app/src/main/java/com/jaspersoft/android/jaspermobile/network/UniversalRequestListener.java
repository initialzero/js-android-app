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

import com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support.RequestExecutor;
import com.octo.android.robospice.persistence.exception.SpiceException;

import org.springframework.http.HttpStatus;

import java.util.EnumMap;

/**
 * @author Tom Koptel
 * @since 1.9.02
 */
public class UniversalRequestListener<T> extends SimpleRequestListener<T> {

    private final EnumMap<HttpStatus, ExceptionRule> rules;
    private final DialogMode dialogMode;
    private final Activity mActivity;
    private final SemanticListener semanticListener;
    private final RequestExecutor.Mode executionMode;

    public UniversalRequestListener(Builder builder) {
        this.mActivity = builder.activity;
        this.rules = builder.rules;
        this.semanticListener = builder.semanticListener;
        this.dialogMode = builder.dialogMode;
        this.executionMode = builder.executionMode;
    }

    public static Builder builder(Activity activity) {
        return new Builder(activity);
    }

    @Override
    public final void onRequestFailure(SpiceException spiceException) {
        boolean finishActivity = (dialogMode == DialogMode.FORCE_CLOSE);
        if (executionMode == RequestExecutor.Mode.VISIBLE) {
            RequestExceptionHandler.handle(spiceException, mActivity, rules, finishActivity);
        }
        if (semanticListener != null) {
            semanticListener.onSemanticFailure(spiceException);
        }
    }

    @Override
    public final void onRequestSuccess(T data) {
        if (semanticListener != null) {
            semanticListener.onSemanticSuccess(data);
        }
    }

    public static class Builder {
        private EnumMap<HttpStatus, ExceptionRule> rules;
        private DialogMode dialogMode;
        private SemanticListener semanticListener;
        private RequestExecutor.Mode executionMode;
        private final Activity activity;

        public Builder(Activity activity) {
            this.activity = activity;
            rules = ExceptionRule.all();
            executionMode = RequestExecutor.Mode.VISIBLE;
            dialogMode = DialogMode.DEFAULT;
        }

        public Builder rules(EnumMap<HttpStatus, ExceptionRule> rules) {
            this.rules = rules;
            return this;
        }

        public Builder closeActivityMode() {
            this.dialogMode = DialogMode.FORCE_CLOSE;
            return this;
        }

        public Builder defaultMode() {
            this.dialogMode = DialogMode.DEFAULT;
            return this;
        }

        public <T> Builder semanticListener(SemanticListener semanticListener) {
            this.semanticListener = semanticListener;
            return this;
        }

        public Builder removeRule(ExceptionRule rule) {
            rules.remove(rule.getHttpStatus());
            return this;
        }

        public Builder executionMode(RequestExecutor.Mode mode) {
            this.executionMode = mode;
            return this;
        }

        public <T> UniversalRequestListener<T> create() {
            return new UniversalRequestListener<T>(this);
        }
    }

    public static enum DialogMode {
        DEFAULT, FORCE_CLOSE
    }

    public static class SimpleSemanticListener<T> implements SemanticListener<T> {
        public void onSemanticFailure(SpiceException spiceException) {
            // Override method for additional implementation
        }

        public void onSemanticSuccess(T data) {
            // Override method for additional implementation
        }
    }

    public static interface SemanticListener<T> {
        void onSemanticFailure(SpiceException spiceException);
        void onSemanticSuccess(T data);
    }
}
