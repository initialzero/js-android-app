package com.jaspersoft.android.jaspermobile;

import com.jaspersoft.android.jaspermobile.internal.di.components.AppComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ProfileComponent;
import com.jaspersoft.android.jaspermobile.internal.di.components.ReportComponent;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface GraphObject {

    AppComponent getComponent();

    void setProfileComponent(ProfileComponent profileComponent);

    void setReportComponent(ReportComponent reportComponent);

    ProfileComponent getProfileComponent();

    ReportComponent getReportComponent();

    void releaseReportComponent();
}
