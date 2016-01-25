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

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboSpiceActivity;
import com.jaspersoft.android.jaspermobile.dialog.DateDialogFragment;
import com.jaspersoft.android.jaspermobile.dialog.OutputFormatDialogFragment;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.rx.RxTransformers;
import com.jaspersoft.android.jaspermobile.widget.DateTimeView;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.network.AuthorizedClient;
import com.jaspersoft.android.sdk.network.Server;
import com.jaspersoft.android.sdk.network.SpringCredentials;
import com.jaspersoft.android.sdk.service.data.schedule.JobData;
import com.jaspersoft.android.sdk.service.rx.report.schedule.RxReportScheduleService;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.CheckedChange;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.util.Calendar;
import java.util.List;

import rx.Subscriber;
import rx.Subscription;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@OptionsMenu(R.menu.report_schedule)
@EActivity(R.layout.activity_schedule)
public class ScheduleActivity extends RoboSpiceActivity implements DateDialogFragment.DateDialogClickListener, OutputFormatDialogFragment.OutputFormatClickListener {

    @Extra
    protected JasperResource jasperResource;

    @Inject
    protected JsRestClient jsRestClient;

    @ViewById(R.id.scheduleName)
    EditText jobName;
    @ViewById(R.id.fileName)
    EditText fileName;
    @ViewById(R.id.ic_boolean_title)
    TextView runImmediatelyTitle;
    @ViewById(R.id.ic_boolean)
    CheckBox runImmediately;
    @ViewById(R.id.scheduleDate)
    DateTimeView scheduleDate;
    @ViewById(R.id.outputFormat)
    TextView outputFormat;

    private Calendar mDate;
    private RxReportScheduleService mService;
    private CompositeSubscription mCompositeSubscription;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initRestClient();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.sch_new);
        }
    }

    private void initRestClient() {
        JsServerProfile serverProfile = jsRestClient.getServerProfile();
        Server server = Server.builder()
                .withBaseUrl(serverProfile.getServerUrl())
                .build();
        SpringCredentials credentials = SpringCredentials.builder()
                .withOrganization(serverProfile.getOrganization())
                .withUsername(serverProfile.getUsername())
                .withPassword(serverProfile.getPassword())
                .build();

        AuthorizedClient client = server.newClient(credentials)
                .withCookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ORIGINAL_SERVER))
                .create();
        mService = RxReportScheduleService.newService(client);

        mCompositeSubscription = new CompositeSubscription();
    }

    @AfterViews
    protected void init() {
        fileName.setText(jasperResource.getLabel());
        runImmediatelyTitle.setText(getString(R.string.sch_run_immediately));

        runImmediately.setChecked(true);
        scheduleDate.setDate(null);
        scheduleDate.setLabel(getString(R.string.sch_start_date));
        scheduleDate.setDateTimeClickListener(new ScheduleDateClickListener());

        outputFormat.setText("PDF");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mCompositeSubscription.unsubscribe();
    }

    @OptionsItem(R.id.newSchedule)
    protected void schedule() {
        subscribe(
                mService.createJob(null)
                        .compose(RxTransformers.<JobData>applySchedulers())
                        .subscribe(new Subscriber<JobData>() {
                            @Override
                            public void onCompleted() {

                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onNext(JobData data) {

                            }
                        })
        );
    }

    @Click(R.id.runImmediately)
    protected void runImmediatelyClicked() {
        runImmediately.performClick();
    }

    @Click(R.id.outputFormat)
    protected void selectOutputFormat() {
        OutputFormatDialogFragment.createBuilder(getSupportFragmentManager())
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
    public void onOutputFormatSelected(String outputFormatTitles) {
        outputFormat.setText(outputFormatTitles);
    }

    private void subscribe(Subscription subscription) {
        mCompositeSubscription.add(subscription);
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
