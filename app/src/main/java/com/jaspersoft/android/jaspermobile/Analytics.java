package com.jaspersoft.android.jaspermobile;

import android.content.Context;

/**
 * @author Andrew Tivodar
 * @since 2.1
 */
public interface Analytics {
    void init(Context appContext);
    void trackPrintEvent();
}
