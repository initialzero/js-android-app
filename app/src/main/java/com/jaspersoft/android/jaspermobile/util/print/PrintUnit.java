package com.jaspersoft.android.jaspermobile.util.print;

import android.os.ParcelFileDescriptor;
import android.print.PageRange;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.1
 */
interface PrintUnit {
    Observable<Integer> getPageCount();
    Observable<Boolean> writeContent(PageRange pageRange, ParcelFileDescriptor destination);
}
