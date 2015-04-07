package com.jaspersoft.android.jaspermobile.test.utils;

import android.accounts.Account;
import android.content.Context;
import android.support.annotation.NonNull;

import com.jaspersoft.android.jaspermobile.JasperMobileApplication;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
public class SavedFilesUtil {

    private final WeakReference<Context> activity;
    private final Account account;

    private SavedFilesUtil(Builder builder) {
        this.activity = new WeakReference<Context>(builder.context);
        this.account = builder.account;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static void deleteSavedItems(Context context) throws IOException {
        File savedReportsDir = getSavedReportsDirectory(context);
        if (savedReportsDir.exists()) {
            org.apache.commons.io.FileUtils.cleanDirectory(savedReportsDir);
        }
    }

    public static File getSavedReportsDirectory(Context context) {
        File appFilesDir = context.getExternalFilesDir(null);
        if (appFilesDir == null) {
            throw new IllegalStateException("Configure external storage on emulator");
        }
        return new File(appFilesDir, JasperMobileApplication.SAVED_REPORTS_DIR_NAME);
    }

    public boolean hasSavedItem(String reportFolderName, String fileFormat) {
        File savedReportsDir = getSavedReportsDirectory(getContext());
        File savedByProfileDir = new File(savedReportsDir, account.name);
        File[] savedReportsFoldersArray = savedByProfileDir.listFiles();
        if (savedReportsFoldersArray == null) return false;

        final String reportFullName = reportFolderName + "." + fileFormat;

        for (File savedReport : savedReportsFoldersArray) {
            if (savedReport.getName().equals(reportFullName)) {
                File[] result = savedReport.listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String filename) {
                        return filename.equals(reportFullName);
                    }
                });
                return result != null && result.length > 0;
            }
        }
        return false;
    }

    @NonNull
    private Context getContext() {
        if (activity.get() == null) {
            throw new IllegalStateException("Missing context");
        }
        return activity.get();
    }

    public static class Builder {
        private Context context;
        private Account account;

        public Builder context(Context context) {
            this.context = context;
            return this;
        }

        public Builder setAccount(Account account) {
            this.account = account;
            return this;
        }

        public SavedFilesUtil build() {
            return new SavedFilesUtil(this);
        }
    }

}
