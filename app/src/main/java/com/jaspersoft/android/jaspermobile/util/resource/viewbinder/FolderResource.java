package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResourceType;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class FolderResource extends JasperResource {

    public FolderResource(String id, String label, String description) {
        super(id, label, description);
    }

    @Override
    public JasperResourceType getResourceType() {
        return JasperResourceType.folder;
    }
}
