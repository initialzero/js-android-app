package com.jaspersoft.android.jaspermobile.test.support.db;

/**
 * @author Tom Koptel
 * @since 2.1
 */
public interface MigrationCondition {
    int oldVersion();
    int newVersion();
}
