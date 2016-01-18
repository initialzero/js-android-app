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

    public SavedItemResource(String id, String label, String description, FileType fileType) {
        super(id, label, description);
        this.fileType = fileType;
    }

    @Override
    public JasperResourceType getResourceType() {
        return JasperResourceType.saved_item;
    }

    public FileType getFileType() {
        return fileType;
    }

}
