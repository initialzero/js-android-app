/*
 * Copyright (C) 2012-2013 Jaspersoft Corporation. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.activities.report;

import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.github.rtyley.android.sherlock.roboguice.activity.RoboSherlockActivity;
import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.JasperMobileApplication;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.SettingsActivity;
import com.jaspersoft.android.jaspermobile.db.DatabaseProvider;
import com.jaspersoft.android.jaspermobile.util.CacheUtils;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.octo.android.robospice.SpiceManager;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import java.io.File;
import java.util.Calendar;

/**
 * @author Ivan Gadzhega
 * @since 1.6
 */
public abstract class BaseReportOptionsActivity extends RoboSherlockActivity {

    // Extras
    public static final String EXTRA_REPORT_LABEL = "ReportOptionsActivity.EXTRA_REPORT_LABEL";
    public static final String EXTRA_REPORT_URI = "ReportOptionsActivity.EXTRA_REPORT_URI";
    // Supported report output formats
    public static final String RUN_OUTPUT_FORMAT_HTML = "HTML";
    public static final String RUN_OUTPUT_FORMAT_PDF = "PDF";
    public static final String RUN_OUTPUT_FORMAT_XLS = "XLS";

    // Action Bar IDs
    private static final int ID_AB_REFRESH = 20;
    private static final int ID_AB_SETTINGS = 21;

    @Inject
    protected JsRestClient jsRestClient;
    @InjectView(R.id.report_format_spinner)
    protected Spinner formatSpinner;
    @InjectView(R.id.runReportButton)
    protected Button runReportButton;

    protected Menu optionsMenu;
    protected DatabaseProvider dbProvider;
    protected SpiceManager serviceManager;
    protected String reportUri;

    private DatePickerDialogHelper dialogHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // workaround for http://bugzilla.jaspersoft.com/show_bug.cgi?id=27735 (only for api 9+)
        int currentApiVersion = Build.VERSION.SDK_INT;
        if (currentApiVersion >= Build.VERSION_CODES.GINGERBREAD){
            permitNetworkOnMainThread();
        }

        setContentView(R.layout.report_options_layout);

        // Get the database provider
        dbProvider = new DatabaseProvider(this);
        // bind to service
        serviceManager = new SpiceManager(JsXmlSpiceService.class);

        // external storage should be writable when using output formats other than HTML
        String[] outputFormats;
        if (isExternalStorageWritable()) {
            outputFormats = new String[] { RUN_OUTPUT_FORMAT_HTML, RUN_OUTPUT_FORMAT_PDF, RUN_OUTPUT_FORMAT_XLS };
        } else {
            outputFormats = new String[] { RUN_OUTPUT_FORMAT_HTML } ;
        }

        // init helper for date/time picker dialogs
        dialogHelper = new DatePickerDialogHelper(this);

        // show spinner with available output formats
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, outputFormats);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        formatSpinner.setAdapter(arrayAdapter);

        // get report label from extras and update title
        String reportLabel = getIntent().getExtras().getString(EXTRA_REPORT_LABEL);
        getSupportActionBar().setTitle(reportLabel);

        // get report uri from extras
        reportUri = getIntent().getExtras().getString(EXTRA_REPORT_URI);
    }

    public abstract void runReportButtonClickHandler(View view);

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        optionsMenu = menu;
        // use the App Icon for Navigation
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // refresh
        MenuItem item = menu.add(Menu.NONE, ID_AB_REFRESH, Menu.NONE, R.string.r_ab_refresh);
        item.setIcon(R.drawable.ic_action_refresh).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        item.setActionView(R.layout.actionbar_indeterminate_progress);
        // settings
        menu.add(Menu.NONE, ID_AB_SETTINGS, Menu.NONE, R.string.ab_settings)
                .setIcon(R.drawable.ic_action_settings).setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case ID_AB_SETTINGS:
                // Launch the settings activity
                Intent settingsIntent = new Intent();
                settingsIntent.setClass(this, SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case android.R.id.home:
                finish();
                return true;
            default:
                // If you don't handle the menu item, you should pass the menu item to the superclass implementation
                return super.onOptionsItemSelected(item);
        }
    }

    protected void setRefreshActionButtonState(boolean refreshing) {
        runReportButton.setEnabled(!refreshing);
        if (optionsMenu != null) {
            MenuItem refreshItem = optionsMenu.findItem(ID_AB_REFRESH);
            if (refreshItem != null) {
                refreshItem.setVisible(refreshing);
                if (refreshing) {
                    refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
                } else {
                    refreshItem.setActionView(null);
                }
            }
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        return dialogHelper.onCreateDialog(id);
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        dialogHelper.onPrepareDialog(id, dialog);
    }

    protected void showDateDialog(InputControlWrapper inputControlWrapper, int id, TextView dateDisplay, Calendar date) {
        dialogHelper.showDateDialog(inputControlWrapper, id, dateDisplay, date);
    }

    protected void showDateDialog(InputControl inputControl, int id, TextView dateDisplay, Calendar date) {
        dialogHelper.showDateDialog(inputControl, id, dateDisplay, date);
    }

    protected void updateDateDisplay(TextView dateDisplay, Calendar date, boolean showTime) {
        dialogHelper.updateDateDisplay(dateDisplay, date, showTime);
    }

    @Override
    protected void onStart() {
        serviceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
        setRefreshActionButtonState(false);
        serviceManager.shouldStop();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        // close any open database object
        if (dbProvider != null) dbProvider.close();
        super.onDestroy();
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    /**
     * Helper Method to Test if external Storage is Available
     * @return <code>true</code> if storage is writable, <code>false</code> otherwise
     */
    private boolean isExternalStorageWritable() {
        String extStorageState = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(extStorageState);
    }

    /**
     * Helper Method to Define the report output cache dir
     * @return directory file
     */
    protected File getReportOutputCacheDir() {
        File cacheDir = (isExternalStorageWritable()) ? CacheUtils.getExternalCacheDir(this) : getCacheDir() ;
        File outputDir = new File(cacheDir, JasperMobileApplication.REPORT_OUTPUT_DIR_NAME);

        if (!outputDir.exists() && !outputDir.mkdirs()){
            Ln.e("Unable to create %s", outputDir);
        }

        return outputDir;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void permitNetworkOnMainThread() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
    }

}