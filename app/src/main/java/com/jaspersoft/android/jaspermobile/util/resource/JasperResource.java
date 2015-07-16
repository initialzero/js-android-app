package com.jaspersoft.android.jaspermobile.util.resource;

import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceType;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class JasperResource {
    private String id;
    private String label;
    private String description;

    public JasperResource(String id, String label, String description) {
        this.id = id;
        this.label = label;
        this.description = description;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public abstract JasperResourceType getResourceType();
}
