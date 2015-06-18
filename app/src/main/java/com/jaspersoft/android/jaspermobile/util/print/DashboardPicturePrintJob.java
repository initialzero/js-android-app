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

package com.jaspersoft.android.jaspermobile.util.print;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.support.v4.print.PrintHelper;
import android.text.TextUtils;
import android.webkit.WebView;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

/**
 * @author Tom Koptel
 * @since 2.1
 */
final class DashboardPicturePrintJob implements ResourcePrintJob {
    private final String printName;
    private final WebView webView;
    private final PrintHelper printHelper;

    private DashboardPicturePrintJob(Builder builder) {
        this.printName = builder.printName;
        this.webView = builder.webView;
        this.printHelper = new PrintHelper(webView.getContext());
    }

    public static Builder builder() {
        return new Builder();
    }

    @TargetApi(19)
    @Override
    public ResourcePrintJob printResource() {
        getScreenShot()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Bitmap>() {
                    @Override
                    public void call(Bitmap bitmap) {
                        printHelper.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                        printHelper.printBitmap(printName, bitmap);
                    }
                });
        return this;
    }

    private Observable<Bitmap> getScreenShot() {
        return Observable.defer(new Func0<Observable<Bitmap>>() {
            @Override
            public Observable<Bitmap> call() {
                return Observable.just(createBitmap());
            }
        });
    }

    private Bitmap createBitmap() {
        Bitmap bitmap = Bitmap.createBitmap(webView.getMeasuredWidth(), webView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        webView.draw(canvas);
        return bitmap;
    }

    public static class Builder {
        private String printName;
        private WebView webView;

        public Builder setPrintName(String printName) {
            this.printName = printName;
            return this;
        }

        public Builder setWebView(WebView webView) {
            this.webView = webView;
            return this;
        }

        public ResourcePrintJob build() {
            validateDependencies();
            return new DashboardPicturePrintJob(this);
        }

        private void validateDependencies() {
            if (webView == null) {
                throw new IllegalStateException("WebView should not be null");
            }
            if (TextUtils.isEmpty(printName)) {
                throw new IllegalStateException("Print name should not be null");
            }
        }
    }
}
