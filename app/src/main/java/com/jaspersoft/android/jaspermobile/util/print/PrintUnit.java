package com.jaspersoft.android.jaspermobile.util.print;

import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.1
 */
interface PrintUnit {
    @NonNull
    Observable<Integer> fetchPageCount();
    @NonNull
    Observable<Boolean> writeContent(String pageRange, ParcelFileDescriptor destination);
}
