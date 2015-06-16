package com.jaspersoft.android.jaspermobile.util.print;

import android.os.ParcelFileDescriptor;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public interface PrintUnit {
    Observable<Integer> getPageCount();
    Observable<Boolean> writeContent(ParcelFileDescriptor destination);
}
