package com.jaspersoft.android.jaspermobile.util.resource;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class UndefinedResource extends JasperResource {

    public UndefinedResource(String id, String label, String description) {
        super(id, label, description);
    }

    @Override
    public JasperResourceType getResourceType() {
        return JasperResourceType.undefined;
    }
}
