package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public interface ResourceSelector {
    boolean isSelected(int position);
    void changeSelectedState(int position);
}
