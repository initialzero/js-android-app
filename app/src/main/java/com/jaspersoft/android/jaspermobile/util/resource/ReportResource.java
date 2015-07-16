package com.jaspersoft.android.jaspermobile.util.resource;

import com.jaspersoft.android.jaspermobile.util.resource.viewbinder.JasperResourceType;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class ReportResource extends JasperResource {

    private String thumbnailUri;

    public ReportResource(String id, String label, String description, String thumbnailUri) {
        super(id, label, description);
        this.thumbnailUri = thumbnailUri;
    }

    @Override
    public JasperResourceType getResourceType() {
        return JasperResourceType.report;
    }

    public String getThumbnailUri() {
        return thumbnailUri;
    }

}
