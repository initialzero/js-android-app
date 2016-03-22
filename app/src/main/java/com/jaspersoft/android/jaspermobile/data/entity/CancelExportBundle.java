package com.jaspersoft.android.jaspermobile.data.entity;

import java.io.File;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class CancelExportBundle {
    private final String mEntityId;
    private final File mFile;

    public CancelExportBundle(String entityId, File file) {
        mEntityId = entityId;
        mFile = file;
    }

    public String getEntityId() {
        return mEntityId;
    }

    public File getFile() {
        return mFile;
    }
}
