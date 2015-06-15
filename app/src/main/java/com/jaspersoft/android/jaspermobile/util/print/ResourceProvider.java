package com.jaspersoft.android.jaspermobile.util.print;

import java.io.File;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public interface ResourceProvider {
    Observable<File> provideResource();
}
