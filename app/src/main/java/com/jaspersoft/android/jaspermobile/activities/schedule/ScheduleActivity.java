/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.schedule;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.GraphObject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.Nullable;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.dialog.DateDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.OutputFormatDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.ValueInputDialogFragment;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.rx.RxTransformers;
import com.jaspersoft.android.jaspermobile.widget.DateTimeView;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.service.data.schedule.JobData;
import com.jaspersoft.android.sdk.service.data.schedule.JobForm;
import com.jaspersoft.android.sdk.service.data.schedule.JobOutputFormat;
import com.jaspersoft.android.sdk.service.data.schedule.JobSimpleTrigger;
import com.jaspersoft.android.sdk.service.data.schedule.JobSource;
import com.jaspersoft.android.sdk.service.data.schedule.RecurrenceIntervalUnit;
import com.jaspersoft.android.sdk.service.data.schedule.RepositoryDestination;
import com.jaspersoft.android.sdk.service.rx.report.schedule.RxReportScheduleService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.TimeZone;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Func1;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@OptionsMenu(R.menu.report_add_schedule)
@EActivity(R.layout.activity_schedule)
public class ScheduleActivity extends RoboToolbarActivity implements DateDialogFragment.DateDialogClickListener,
        OutputFormatDialogFragment.OutputFormatClickListener, ValueInputDialogFragment.ValueDialogCallback {

    private final static int JOB_NAME_CODE = 563;
    private final static int FILE_NAME_CODE = 251;
    private final static int OUTPUT_PATH_CODE = 515;
    private final static String DEFAULT_OUTPUT_PATH = "/public/Samples/Reports";

    @Extra
    protected JasperResource jasperResource;

    @Inject
    @Nullable
    protected Analytics analytics;
    @Inject
    @Nullable
    protected JasperRestClient mRestClient;

    @ViewById(R.id.scheduleName)
    TextView jobName;
    @ViewById(R.id.fileName)
    TextView fileName;
    @ViewById(R.id.ic_boolean_title)
    TextView runImmediatelyTitle;
    @ViewById(R.id.ic_boolean)
    CheckBox runImmediately;
    @ViewById(R.id.scheduleDate)
    DateTimeView scheduleDate;
    @ViewById(R.id.outputFormat)
    TextView outputFormat;
    @ViewById(R.id.outputPath)
    TextView outputPath;

    private Calendar mDate;
    private ArrayList<JobOutputFormat> mFormats;
    private CompositeSubscription mCompositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GraphObject.Factory.from(this)
                .getProfileComponent()
                .inject(this);

        mCompositeSubscription = new CompositeSubscription();

        mFormats = new ArrayList<>();
        mFormats.add(JobOutputFormat.PDF);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.sch_new);
        }
    }

    @AfterViews
    protected void init() {
        jobName.setText(R.string.sch_new);

        String outputFileName = jasperResource.getLabel().replace(" ", "_");
        fileName.setText(outputFileName);
        runImmediatelyTitle.setText(getString(R.string.sch_run_immediately));

        runImmediately.setChecked(true);
        scheduleDate.setDate(null);
        scheduleDate.setLabel(getString(R.string.sch_start_date));
        scheduleDate.setDateTimeClickListener(new ScheduleDateClickListener());

        outputFormat.setText(getSupportedFormatsTitles());
        outputPath.setText(DEFAULT_OUTPUT_PATH);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCompositeSubscription.unsubscribe();
    }

    @Override
    protected String getScreenName() {
        return getString(R.string.ja_sch);
    }

    @OptionsItem(R.id.addSchedule)
    protected void schedule() {
        ProgressDialogFragment.builder(getSupportFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mCompositeSubscription.unsubscribe();
                        mCompositeSubscription = new CompositeSubscription();
                    }
                })
                .show();
        subscribe(
                mRestClient.scheduleService()
                        .flatMap(new Func1<RxReportScheduleService, Observable<JobData>>() {
                            @Override
                            public Observable<JobData> call(RxReportScheduleService rxReportScheduleService) {
                                return rxReportScheduleService.createJob(createJobForm());
                            }
                        })
                        .compose(RxTransformers.<JobData>applySchedulers())
                        .subscribe(new Subscriber<JobData>() {
                            @Override
                            public void onCompleted() {
                                analytics.sendEvent(Analytics.EventCategory.RESOURCE.getValue(), Analytics.EventAction.SCHEDULED.getValue(), null);
                            }

                            @Override
                            public void onError(Throwable e) {
                                ProgressDialogFragment.dismiss(getSupportFragmentManager());
                                // TODO: handle error
                            }

                            @Override
                            public void onNext(JobData data) {
                                ProgressDialogFragment.dismiss(getSupportFragmentManager());
                                Toast.makeText(ScheduleActivity.this, R.string.sch_created, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
        );
    }

    @Click(R.id.runImmediately)
    protected void runImmediatelyClicked() {
        runImmediately.performClick();
    }

    @Click(R.id.scheduleNameContainer)
    protected void scheduleNameClick() {
        ValueInputDialogFragment.createBuilder(getSupportFragmentManager())
                .setLabel(getString(R.string.sch_job_name))
                .setValue(jobName.getText().toString())
                .setCancelableOnTouchOutside(true)
                .setRequestCode(JOB_NAME_CODE)
                .show();
    }

    @Click(R.id.fileNameContainer)
    protected void fileNameClick() {
        ValueInputDialogFragment.createBuilder(getSupportFragmentManager())
                .setLabel(getString(R.string.sch_file_name))
                .setValue(fileName.getText().toString())
                .setCancelableOnTouchOutside(true)
                .setRequestCode(FILE_NAME_CODE)
                .show();
    }

    @Click(R.id.outputFormatContainer)
    protected void selectOutputFormat() {
        OutputFormatDialogFragment.createBuilder(getSupportFragmentManager())
                .setSelectedFormats(mFormats)
                .show();
    }

    @Click(R.id.outputPathContainer)
    protected void outputPathClick() {
        ValueInputDialogFragment.createBuilder(getSupportFragmentManager())
                .setLabel(getString(R.string.sch_destination))
                .setValue(outputPath.getText().toString())
                .setCancelableOnTouchOutside(true)
                .setRequestCode(OUTPUT_PATH_CODE)
                .show();
    }

    @CheckedChange(R.id.ic_boolean)
    protected void checkBoxCheckedChange(boolean checked) {
        runImmediately.setChecked(checked);
        scheduleDate.setVisibility(checked ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDateSelected(String id, Calendar date) {
        mDate = date;
        scheduleDate.setDate(mDate);
    }

    @Override
    public void onTextValueEntered(int requestCode, String name) {
        switch (requestCode) {
            case JOB_NAME_CODE:
                jobName.setText(name);
                break;
            case FILE_NAME_CODE:
                fileName.setText(name);
                break;
            case OUTPUT_PATH_CODE:
                outputPath.setText(name);
                break;
        }
    }

    @Override
    public void onOutputFormatSelected(ArrayList<JobOutputFormat> outputFormats) {
        mFormats = outputFormats;
        outputFormat.setText(getSupportedFormatsTitles());
    }

    private void subscribe(Subscription subscription) {
        mCompositeSubscription.add(subscription);
    }

    private String getSupportedFormatsTitles() {
        return mFormats.isEmpty() ? InputControlWrapper.NOTHING_SUBSTITUTE_LABEL : TextUtils.join(", ", mFormats);
    }

    private JobForm createJobForm() {
        JobSimpleTrigger jobTrigger = new JobSimpleTrigger.Builder()
                .withOccurrenceCount(1)
                .withRecurrenceIntervalUnit(RecurrenceIntervalUnit.WEEK)
                .withRecurrenceInterval(0)
                .withTimeZone(TimeZone.getDefault())
                .withStartDate(mDate == null ? null : mDate.getTime())
                .build();

        return new JobForm.Builder()
                .withBaseOutputFilename(fileName.getText().toString())
                .withLabel(jobName.getText().toString())
                .withSimpleTrigger(jobTrigger)
                .withJobSource(new JobSource.Builder().withUri(jasperResource.getId()).build())
                .withRepositoryDestination(new RepositoryDestination.Builder().withFolderUri(outputPath.getText().toString()).build())
                .addOutputFormats(mFormats)
                .build();
    }

    private class ScheduleDateClickListener implements DateTimeView.DateTimeClickListener {
        @Override
        public void onDateClick(int position) {
            DateDialogFragment.createBuilder(getSupportFragmentManager())
                    .setInputControlId(null)
                    .setDate(mDate)
                    .setType(DateDialogFragment.DATE)
                    .show();
        }

        @Override
        public void onTimeClick(int position) {
            DateDialogFragment.createBuilder(getSupportFragmentManager())
                    .setInputControlId(null)
                    .setDate(mDate)
                    .setType(DateDialogFragment.TIME)
                    .show();
        }

        @Override
        public void onClear(int position) {
            mDate = null;
            scheduleDate.setDate(null);
        }
    }
}
