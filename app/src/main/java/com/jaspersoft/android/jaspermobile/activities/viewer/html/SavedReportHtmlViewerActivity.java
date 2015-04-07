/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.viewer.html;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.db.provider.JasperMobileDbProvider;
import com.jaspersoft.android.jaspermobile.dialog.DeleteDialogFragment;
import com.jaspersoft.android.sdk.util.FileUtils;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.io.File;

/**
 * Activity that performs report viewing in HTML format.
 *
 * @author Ivan Gadzhega
 * @since 1.4
 */

@EActivity
@OptionsMenu(R.menu.saved_report)
public class SavedReportHtmlViewerActivity extends RoboToolbarActivity
        implements WebViewFragment.OnWebViewCreated, DeleteDialogFragment.DeleteDialogClickListener {

    @Extra
    File reportFile;

    @Extra
    long reportId;

    @Extra
    String resourceLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            WebViewFragment webViewFragment = WebViewFragment_.builder()
                    .resourceLabel(resourceLabel).build();
            webViewFragment.setOnWebViewCreated(this);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, webViewFragment, WebViewFragment.TAG)
                    .commit();
        }
    }

    @Override
    public void onWebViewCreated(WebViewFragment webViewFragment) {
        Uri reportOutputPath = Uri.fromFile(reportFile);
        webViewFragment.loadUrl(reportOutputPath.toString());
    }

    @OptionsItem
    final void deleteItem() {
        Uri uri = Uri.withAppendedPath(JasperMobileDbProvider.SAVED_ITEMS_CONTENT_URI,
                String.valueOf(reportId));

        DeleteDialogFragment.createBuilder(this, getSupportFragmentManager())
                .setFile(reportFile)
                .setRecordUri(uri)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle(R.string.sdr_drd_title)
                .setMessage(getString(R.string.sdr_drd_msg, resourceLabel))
                .setPositiveButtonText(R.string.spm_delete_btn)
                .setNegativeButtonText(android.R.string.cancel)
                .show();
    }

    //---------------------------------------------------------------------
    // Implements Implements DeleteDialogFragment.DeleteDialogClickListener
    //---------------------------------------------------------------------

    @Override
    public void onDeleteConfirmed(Uri itemToDelete, File fileToDelete) {
        if (fileToDelete.isDirectory()) {
            FileUtils.deleteFilesInDirectory(fileToDelete);
        }

        if (fileToDelete.delete() || !fileToDelete.exists()) {
            getContentResolver().delete(itemToDelete, null, null);
        } else {
            Toast.makeText(this, R.string.sdr_t_report_deletion_error, Toast.LENGTH_SHORT).show();
        }

        finish();
    }

    @Override
    public void onDeleteCanceled() {

    }
}
