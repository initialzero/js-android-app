package com.jaspersoft.android.jaspermobile.util.print;

import android.os.ParcelFileDescriptor;
import android.print.PageRange;
import android.support.annotation.NonNull;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.1
 */
interface PrintUnit {
    @NonNull
    Observable<Integer> getPageCount();
    @NonNull
    Observable<Boolean> writeContent(PageRange pageRange, ParcelFileDescriptor destination);
}
