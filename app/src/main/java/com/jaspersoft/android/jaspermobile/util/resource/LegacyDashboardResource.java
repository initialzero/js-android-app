package com.jaspersoft.android.jaspermobile.util.resource;

/**
 * TODO revise Dashboard conception. Do we need this?
 *
 * @author Tom Koptel
 * @since 2.3
 */
public class LegacyDashboardResource extends DashboardResource {
    public LegacyDashboardResource(String id, String label, String description) {
        super(id, label, description);
    }

    @Override
    public JasperResourceType getResourceType() {
        return JasperResourceType.legacyDashboard;
    }
}
