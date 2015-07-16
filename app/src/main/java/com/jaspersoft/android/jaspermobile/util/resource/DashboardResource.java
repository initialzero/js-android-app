package com.jaspersoft.android.jaspermobile.util.resource;

import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceType;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class DashboardResource extends JasperResource {

    public DashboardResource(String id, String label, String description) {
        super(id, label, description);
    }

    @Override
    public JasperResourceType getResourceType() {
        return JasperResourceType.dashboard;
    }
}
