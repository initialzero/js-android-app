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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support;

import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;

import com.google.common.base.Preconditions;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.request.CachedSpiceRequest;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

/**
 * @author Tom Koptel
 * @since 1.9.1
 */
public class RequestExecutor {
    private final SpiceManager spiceManager;
    private final FragmentManager fragmentManager;
    private final Mode executionMode;

    public static Builder builder() {
        return new Builder();
    }

    private RequestExecutor(SpiceManager spiceManager,
                            FragmentManager fragmentManager,
                            Mode executionMode) {
        this.spiceManager = spiceManager;
        this.fragmentManager = fragmentManager;
        this.executionMode = executionMode;
    }

    public <T> void execute(final SpiceRequest<T> request, final RequestListener<T> requestListener) {
        final CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<T>(request, null, DurationInMillis.ALWAYS_RETURNED);
        execute(cachedSpiceRequest, requestListener);
    }

    public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey, final long cacheExpiryDuration, final RequestListener<T> requestListener) {
        final CachedSpiceRequest<T> cachedSpiceRequest = new CachedSpiceRequest<T>(request, requestCacheKey, cacheExpiryDuration);
        execute(cachedSpiceRequest, requestListener);
    }

    public <T> void execute(CachedSpiceRequest<T> request, RequestListener<T> requestListener) {
        boolean isVisibleExecutionInProgress = ProgressDialogFragment.isVisible(fragmentManager);
        if (isVisibleExecutionInProgress) {
            makeSilentExecution(request, requestListener);
            return;
        }

        switch (executionMode) {
            case SILENT:
                makeSilentExecution(request, requestListener);
                break;
            case VISIBLE:
                makeVisibleExecution(request, requestListener);
                break;
            default:
                throw new UnsupportedOperationException();
        }
    }

    private <T> void makeSilentExecution(SpiceRequest<T> request, RequestListener<T> requestListener) {
        spiceManager.execute(request, requestListener);
    }

    private <T> void makeVisibleExecution(final SpiceRequest<T> request, final RequestListener<T> requestListener) {
        DialogInterface.OnCancelListener cancelListener = new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (!request.isCancelled()) {
                    spiceManager.cancel(request);
                }
            }
        };
        DialogInterface.OnShowListener showListener = new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                spiceManager.execute(request, requestListener);
            }
        };

        ProgressDialogFragment.builder(fragmentManager)
                .setOnCancelListener(cancelListener)
                .setOnShowListener(showListener)
                .show();
    }

    public boolean runsInSilentMode() {
        return executionMode == Mode.SILENT;
    }

    public Mode getExecutionMode() {
        return executionMode;
    }

    public static class Builder {
        private SpiceManager spiceManager;
        private FragmentManager fragmentManager;
        private Mode executionMode;

        public Builder() {
            this.executionMode = Mode.VISIBLE;
        }

        public Builder setSpiceManager(SpiceManager spiceManager) {
            this.spiceManager = spiceManager;
            return this;
        }

        public Builder setFragmentManager(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
            return this;
        }

        public Builder setExecutionMode(Mode executionMode) {
            this.executionMode = executionMode;
            return this;
        }

        public RequestExecutor create() {
            Preconditions.checkNotNull(spiceManager);
            Preconditions.checkNotNull(fragmentManager);
            return new RequestExecutor(spiceManager, fragmentManager, executionMode);
        }
    }

    public static enum Mode {
        SILENT, VISIBLE
    }
}
