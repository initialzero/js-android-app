package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class FolderResource extends JasperResource {

    public FolderResource(String label, String description) {
        super(label, description);
    }

    @Override
    public JasperResourceType getResourceType() {
        return JasperResourceType.folder;
    }
}
