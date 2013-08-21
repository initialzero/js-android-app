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
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.view.View;
import android.widget.*;
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
import com.jaspersoft.android.sdk.client.oxm.ResourceDescriptor;
import com.jaspersoft.android.sdk.client.oxm.ResourceParameter;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule;
import com.octo.android.robospice.SpiceManager;
import roboguice.inject.InjectView;
import roboguice.util.Ln;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * @author Ivan Gadzhega
 * @since 1.5.2
 */
public abstract class BaseReportOptionsActivity extends RoboSherlockActivity {

    // Extras
    public static final String EXTRA_REPORT_LABEL = "ReportOptionsActivity.EXTRA_REPORT_LABEL";
    public static final String EXTRA_REPORT_URI = "ReportOptionsActivity.EXTRA_REPORT_URI";
    // Supported report output formats
    public static final String RUN_OUTPUT_FORMAT_HTML = "HTML";
    public static final String RUN_OUTPUT_FORMAT_PDF = "PDF";
    public static final String RUN_OUTPUT_FORMAT_XLS = "XLS";
    // date format
    protected static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    // Dialog IDs
    protected static final int DATE_DIALOG_ID = 10;
    protected static final int TIME_DIALOG_ID = 11;
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

    private TextView activeDateDisplay;
    private Calendar activeDate;
    private InputControlWrapper activeInputControlWrapper;
    private InputControl activeInputControl;

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
        switch (id) {
            case DATE_DIALOG_ID:
                return new DatePickerDialog(this, dateSetListener, activeDate.get(Calendar.YEAR), activeDate.get(Calendar.MONTH), activeDate.get(Calendar.DAY_OF_MONTH));
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, timeSetListener, activeDate.get(Calendar.HOUR_OF_DAY), activeDate.get(Calendar.MINUTE), true);
        }
        return null;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id) {
            case DATE_DIALOG_ID:
                ((DatePickerDialog) dialog).updateDate(activeDate.get(Calendar.YEAR), activeDate.get(Calendar.MONTH), activeDate.get(Calendar.DAY_OF_MONTH));
                break;
            case TIME_DIALOG_ID:
                ((TimePickerDialog) dialog).updateTime(activeDate.get(Calendar.HOUR_OF_DAY), activeDate.get(Calendar.MINUTE));
                break;
        }
    }

    protected void showDateDialog(int id, InputControlWrapper inputControlWrapper, InputControl InputControl,
                                  TextView dateDisplay, Calendar date) {
        activeInputControlWrapper = inputControlWrapper;
        activeInputControl = InputControl;
        activeDateDisplay = dateDisplay;
        activeDate = date;
        showDialog(id);
    }

    protected void updateDateDisplay(TextView dateDisplay, Calendar date, boolean showTime) {
        String displayText;
        if(showTime) {
            displayText = DateFormat.getDateTimeInstance().format(date.getTime());
        } else {
            displayText = DateFormat.getDateInstance().format(date.getTime());
        }
        dateDisplay.setText(displayText);

    }

    @Override
    protected void onStart() {
        serviceManager.start(this);
        super.onStart();
    }

    @Override
    protected void onStop() {
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

    private DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            activeDate.set(Calendar.YEAR, year);
            activeDate.set(Calendar.MONTH, monthOfYear);
            activeDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateDisplayAndValueOnDateSet();
        }
    };

    private TimePickerDialog.OnTimeSetListener timeSetListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            activeDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            activeDate.set(Calendar.MINUTE, minute);
            updateDisplayAndValueOnDateSet();

        }
    };

    private void updateDisplayAndValueOnDateSet() {
        if (activeInputControlWrapper != null) {
            boolean isDateTime = (activeInputControlWrapper.getDataType() == ResourceDescriptor.DT_TYPE_DATE_TIME);
            updateDateDisplay(activeDateDisplay, activeDate, isDateTime);
            // update control
            List<ResourceParameter> parameters = new ArrayList<ResourceParameter>();
            parameters.add(new ResourceParameter(activeInputControlWrapper.getName(), String.valueOf(activeDate.getTimeInMillis()), false));
            activeInputControlWrapper.setListOfSelectedValues(parameters);
        } else if (activeInputControl != null) {
            String format = DEFAULT_DATE_FORMAT;
            for (DateTimeFormatValidationRule validationRule : activeInputControl.getValidationRules(DateTimeFormatValidationRule.class)) {
                format = validationRule.getFormat();
            }
            DateFormat formatter = new SimpleDateFormat(format);
            String date = formatter.format(activeDate.getTime()) ;
            activeDateDisplay.setText(date);
            activeInputControl.getState().setValue(date);
        }
        unregisterDateDisplay();
    }

    private void unregisterDateDisplay() {
        activeDateDisplay = null;
        activeDate = null;
        activeInputControlWrapper = null;
        activeInputControl = null;
    }

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    private void permitNetworkOnMainThread() {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitNetwork().build();
        StrictMode.setThreadPolicy(policy);
    }

}