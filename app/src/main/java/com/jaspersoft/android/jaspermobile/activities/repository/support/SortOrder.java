package com.jaspersoft.android.jaspermobile.activities.repository.support;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public enum SortOrder {
    LABEL("label"), CREATION_DATE("creationDate");

    private final String mValue;

    private SortOrder(String value) {
        mValue = value;
    }

    public String getValue() {
        return mValue;
    }
}
