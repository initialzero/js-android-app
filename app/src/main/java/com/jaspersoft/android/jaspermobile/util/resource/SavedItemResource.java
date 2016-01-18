package com.jaspersoft.android.jaspermobile.util.resource;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class SavedItemResource extends JasperResource {

    public enum FileType {
        HTML,
        PDF,
        XLS,
        UNKNOWN;

        public static FileType getValueOf(String extension) {
            try {
                return FileType.valueOf(extension);
            } catch (IllegalArgumentException ex) {
                return FileType.UNKNOWN;
            }
        }
    }

    private FileType fileType;
    private boolean downloaded;

    public SavedItemResource(String id, String label, String description, FileType fileType, boolean downloaded) {
        super(id, label, description);
        this.fileType = fileType;
        this.downloaded = downloaded;
    }

    @Override
    public JasperResourceType getResourceType() {
        return JasperResourceType.saved_item;
    }

    public FileType getFileType() {
        return fileType;
    }

    public boolean isDownloaded() {
        return downloaded;
    }
}
