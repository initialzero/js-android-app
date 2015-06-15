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
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import rx.functions.Action1;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public final class CommonPrintJob implements ResourcePrintJob {
    private final Context mContext;

    private CommonPrintJob(Context context) {
        mContext = context;
    }

    public static ResourcePrintJob newInstance(Context context) {
        return new CommonPrintJob(context);
    }

    @Override
    public Action1<File> printResource() {
        return new Action1<File>() {
            @Override
            public void call(File file) {
                showPrintPreview(file);
            }
        };
    }

    @Override
    public Action1<Throwable> reportError() {
        return new Action1<Throwable>() {
            @Override
            public void call(Throwable throwable) {
                Toast.makeText(mContext, throwable.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
    }

    @TargetApi(19)
    private void showPrintPreview(File fileToPrint) {
        PrintManager printManager = (PrintManager) mContext.getSystemService(Context.PRINT_SERVICE);
        String jobName = fileToPrint.getName();

        PrintAttributes printAttributes = new PrintAttributes.Builder().build();
        PrintDocumentAdapter printAdapter = new PrintReportAdapter(fileToPrint);

        printManager.print(jobName, printAdapter, printAttributes);
    }

    @TargetApi(19)
    private static class PrintReportAdapter extends PrintDocumentAdapter {
        private File fileToPrint;

        public PrintReportAdapter(File fileToPrint) {
            this.fileToPrint = fileToPrint;
        }

        @Override
        public void onLayout(PrintAttributes oldAttributes, PrintAttributes newAttributes, CancellationSignal cancellationSignal, LayoutResultCallback callback, Bundle extras) {
            if (cancellationSignal.isCanceled()) {
                callback.onLayoutCancelled();
                return;
            }

            PrintDocumentInfo pdi = new PrintDocumentInfo.Builder(fileToPrint.getName())
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .build();
            callback.onLayoutFinished(pdi, true);
        }

        @Override
        public void onWrite(PageRange[] pages, ParcelFileDescriptor destination, CancellationSignal cancellationSignal, WriteResultCallback callback) {
            InputStream input = getFileInputStream();
            OutputStream output = new FileOutputStream(destination.getFileDescriptor());
            try {
                IOUtils.copy(input, output);
                callback.onWriteFinished(new PageRange[]{PageRange.ALL_PAGES});
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                IOUtils.closeQuietly(input);
                IOUtils.closeQuietly(output);
            }
        }

        private InputStream getFileInputStream() {
            try {
                return new FileInputStream(fileToPrint);
            } catch (FileNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
