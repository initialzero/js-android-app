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

package com.jaspersoft.android.jaspermobile.util.print;

import android.annotation.TargetApi;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.text.TextUtils;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@TargetApi(19)
final class PrintReportAdapter extends PrintDocumentAdapter {
    private Subscription getPageCountTask;
    private Subscription writeContentTask;
    private final String printName;
    private final PrintUnit printUnit;
    private PageRangeFormat mPageRangeFormat;

    PrintReportAdapter(PrintUnit printUnit, String printName) {
        if (printUnit == null) {
            throw new IllegalArgumentException("Print unit should not be null");
        }
        if (TextUtils.isEmpty(printName)) {
            throw new IllegalArgumentException("Print name should not be null");
        }

        this.printUnit = printUnit;
        this.printName = printName;
    }

    @Override
    public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes,
                         CancellationSignal cancellationSignal, final LayoutResultCallback callback, Bundle extras) {
        if (cancellationSignal.isCanceled()) {
            cancelGetCountTask();
            callback.onLayoutCancelled();
            return;
        }

        getPageCountTask = printUnit.fetchPageCount()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Integer>() {
                            @Override
                            public void call(Integer pageCount) {
                                mPageRangeFormat = new PageRangeFormat(pageCount);
                                PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(printName)
                                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                                        .setPageCount(pageCount)
                                        .build();
                                callback.onLayoutFinished(pdi, true);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                callback.onLayoutFailed(throwable.getMessage());
                            }
                        });
    }

    @Override
    public void onWrite(final PageRange[] pages, final ParcelFileDescriptor destination,
                        CancellationSignal cancellationSignal, final WriteResultCallback callback) {
        if (cancellationSignal.isCanceled()) {
            cancelWriteTask();
            callback.onWriteCancelled();
            return;
        }

        String range = mPageRangeFormat.format(pages[0]);
        writeContentTask = printUnit.writeContent(range, destination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        new Action1<Object>() {
                            @Override
                            public void call(Object o) {
                                callback.onWriteFinished(pages);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                callback.onWriteFailed(throwable.getMessage());
                            }
                        });
    }

    @Override
    public void onFinish() {
        cancelGetCountTask();
        cancelWriteTask();
    }

    private void cancelGetCountTask() {
        if (getPageCountTask != null) {
            getPageCountTask.unsubscribe();
        }
    }

    private void cancelWriteTask() {
        if (writeContentTask != null) {
            writeContentTask.unsubscribe();
        }
    }
}