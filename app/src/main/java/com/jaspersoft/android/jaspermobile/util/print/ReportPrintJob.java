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
import android.content.Context;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintDocumentInfo;
import android.print.PrintManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;
import org.roboguice.shaded.goole.common.annotations.VisibleForTesting;
import org.springframework.http.client.ClientHttpResponse;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public class ReportPrintJob implements ResourcePrintJob {
    private final Context mContext;
    private final ResourceProvider<Observable<ClientHttpResponse>> resourceProvider;
    private final String printName;

    private ReportPrintJob(@NonNull Builder builder) {
        mContext = builder.context;
        resourceProvider = builder.resourceProvider;
        printName = builder.printName;
    }

    public static Builder builder(Context context) {
        return new Builder(context);
    }

    @TargetApi(19)
    @Override
    public void printResource() {
        PrintManager printManager = (PrintManager) mContext.getSystemService(Context.PRINT_SERVICE);
        String jobName = printName;

        PrintAttributes printAttributes = new PrintAttributes.Builder().build();
        PrintDocumentAdapter printAdapter = new PrintReportAdapter();

        printManager.print(jobName, printAdapter, printAttributes);
    }

    @TargetApi(19)
    private class PrintReportAdapter extends PrintDocumentAdapter {
        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(printName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build();
            callback.onLayoutFinished(pdi, true);
        }

        @Override
        public void onWrite(PageRange[] pages, final ParcelFileDescriptor destination, CancellationSignal cancellationSignal, final WriteResultCallback callback) {
            copyContent(destination)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            new Action1<Object>() {
                                @Override
                                public void call(Object o) {
                                    callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
                                }
                            }, new Action1<Throwable>() {
                                @Override
                                public void call(Throwable throwable) {
                                    Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
        }

        private Observable<Object> copyContent(ParcelFileDescriptor destination) {
            return Observable.zip(resourceProvider.provideResource(), Observable.just(destination), new Func2<ClientHttpResponse, ParcelFileDescriptor, Object>() {
                @Override
                public Object call(ClientHttpResponse clientHttpResponse, ParcelFileDescriptor parcelFileDescriptor) {
                    blockingCopyContent(clientHttpResponse, parcelFileDescriptor);
                    return null;
                }
            });
        }

        private void blockingCopyContent(final ClientHttpResponse httpResponse, final ParcelFileDescriptor destination) {
            OutputStream output = new FileOutputStream(destination.getFileDescriptor());
            InputStream inputStream = null;
            try {
                inputStream = httpResponse.getBody();
                IOUtils.copy(httpResponse.getBody(), output);
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                if (inputStream != null) {
                    IOUtils.closeQuietly(inputStream);
                }
                IOUtils.closeQuietly(output);
                httpResponse.close();
            }
        }
    }

    public static class Builder {
        private final Context context;
        private String printName;
        private ResourceProvider<Observable<ClientHttpResponse>> resourceProvider;

        public Builder(Context context) {
            this.context = context;
        }

        @VisibleForTesting
        Builder setObservableResourceProvider(ResourceProvider<Observable<ClientHttpResponse>> resourceProvider) {
            this.resourceProvider = resourceProvider;
            return this;
        }

        public Builder setResourceProvider(@Nullable ResourceProvider<ClientHttpResponse> resourceProvider) {
            this.resourceProvider = StreamResourceProviderDecorator.decorate(resourceProvider);
            return this;
        }

        public Builder setPrintName(@Nullable String printName) {
            this.printName = printName;
            return this;
        }

        public ResourcePrintJob build() {
            validateDependencies();
            return new ReportPrintJob(this);
        }

        private void validateDependencies() {
            if (resourceProvider == null) {
                throw new IllegalStateException("Resource provider should not be null");
            }
            if (TextUtils.isEmpty(printName)) {
                throw new IllegalStateException("Job print name should not be null. Current value: " + printName);
            }
        }
    }
}
