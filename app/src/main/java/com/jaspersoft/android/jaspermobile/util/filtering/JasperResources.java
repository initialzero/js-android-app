package com.jaspersoft.android.jaspermobile.util.filtering;

import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.util.ArrayList;

import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.dashboard;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.folder;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.legacyDashboard;
import static com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup.ResourceType.reportUnit;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class JasperResources {

    public static ArrayList<String> folder() {
        return JasperFilter.FOLDER.getAsList();
    }

    public static ArrayList<String> report() {
        return JasperFilter.REPORT.getAsList();
    }

    public static ArrayList<String> dashboard(ServerRelease serverRelease) {
        boolean isPreAmber = serverRelease.code() < ServerRelease.AMBER.code();
        if (isPreAmber) {
            return JasperFilter.DASHBOARD_PRE_AMBER.getAsList();
        } else {
            return JasperFilter.DASHBOARD_AMBER.getAsList();
        }
    }

    private enum JasperFilter {
        FOLDER(folder),
        REPORT(reportUnit),
        DASHBOARD_PRE_AMBER(dashboard),
        DASHBOARD_AMBER(legacyDashboard, dashboard);

        private final ArrayList<String> mTypes = new ArrayList<String>();

        JasperFilter(ResourceLookup.ResourceType... types) {
            for (ResourceLookup.ResourceType type : types) {
                mTypes.add(type.toString());
            }
        }

        ArrayList<String> getAsList() {
            return mTypes;
        }
    }
}
