package com.jaspersoft.android.jaspermobile.test.utils;

import android.content.Context;

import com.google.common.base.Preconditions;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;

import java.io.File;
import java.io.IOException;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
public class SavedFilesUtil {

    private SavedFilesUtil() {
        throw new AssertionError();
    }

    public static void clear(Context context) throws IOException {
        File savedReportsDir = getSavedReportsDirectory(context);
        if (savedReportsDir.exists()) {
            org.apache.commons.io.FileUtils.cleanDirectory(savedReportsDir);
        }
    }

    public static boolean contains(Context context, String reportFolderName, String fileFormat, long profileId) {
        File savedReportsDir = getSavedReportsDirectory(context);
        File savedByProfileDir = new File(savedReportsDir, String.valueOf(profileId));
        File[] savedReportsFoldersArray = savedByProfileDir.listFiles();
        if (savedReportsFoldersArray == null) return false;

        String reportFullName = reportFolderName + "." + fileFormat;

        for (File savedReport : savedReportsFoldersArray) {
            if (savedReport.getName().equals(reportFolderName)) {
                for (File savedFilesInReport : savedReport.listFiles()) {
                    if (savedFilesInReport.getName().equals(reportFullName))
                        return true;
                }
            }
        }
        return false;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------
    private static File getSavedReportsDirectory(Context context) {
        Preconditions.checkNotNull(context, "Call startActivityUnderTest() before this method");
        File appFilesDir = context.getExternalFilesDir(null);
        Preconditions.checkNotNull(appFilesDir, "Configure external storage on emulator");
        return new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);
    }
}
