package com.jaspersoft.android.jaspermobile;

import android.app.Application;

/**
 * @author Andrew Tivodar
 * @since 2.1
 */
public interface Analytics {
    void init(Application appContext);
    void trackPrintEvent(PrintType printType);

    enum PrintType{
        REPORT,
        DASHBOARD
    }
}
