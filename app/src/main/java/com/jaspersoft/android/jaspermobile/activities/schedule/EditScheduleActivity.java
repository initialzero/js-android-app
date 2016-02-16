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
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.Analytics;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;
import com.jaspersoft.android.jaspermobile.network.RequestExceptionHandler;
import com.jaspersoft.android.jaspermobile.presentation.view.activity.ToolbarActivity;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.rx.RxTransformers;
import com.jaspersoft.android.jaspermobile.util.schedule.JobConverter;
import com.jaspersoft.android.jaspermobile.util.schedule.ScheduleViewModel;
import com.jaspersoft.android.sdk.service.data.schedule.JobData;
import com.jaspersoft.android.sdk.service.data.schedule.JobForm;
import com.jaspersoft.android.sdk.service.data.schedule.JobOutputFormat;
import com.jaspersoft.android.sdk.service.data.schedule.JobSimpleTrigger;
import com.jaspersoft.android.sdk.service.data.schedule.JobSource;
import com.jaspersoft.android.sdk.service.data.schedule.RecurrenceIntervalUnit;
import com.jaspersoft.android.sdk.service.data.schedule.RepositoryDestination;
import com.jaspersoft.android.sdk.service.rx.report.schedule.RxReportScheduleService;

import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

import java.util.ArrayList;
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
@OptionsMenu(R.menu.report_edit_schedule)
@EActivity
public class EditScheduleActivity extends ToolbarActivity {

    @Extra
    protected int jobId;

    @Inject
    protected Analytics analytics;
    @Inject
    protected JasperRestClient mRestClient;

    private CompositeSubscription mCompositeSubscription;
    private ScheduleFragment mScheduleFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getProfileComponent().inject(this);

        mCompositeSubscription = new CompositeSubscription();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(R.string.sch_edit);
        }

        if (savedInstanceState != null) return;

        requestJobInfo();
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

    @OptionsItem(R.id.editSchedule)
    protected void editSchedule() {
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
                                ScheduleViewModel scheduleViewModel = mScheduleFragment.provideJob();
                                return rxReportScheduleService.createJob(JobConverter.toJobForm(scheduleViewModel));
                            }
                        })
                        .compose(RxTransformers.<JobData>applySchedulers())
                        .subscribe(new Subscriber<JobData>() {
                            @Override
                            public void onCompleted() {
                                hideLoading();
                                analytics.sendEvent(Analytics.EventCategory.JOB.getValue(), Analytics.EventAction.CHANGED.getValue(), null);
                            }

                            @Override
                            public void onError(Throwable e) {
                                hideLoading();
                                RequestExceptionHandler.showAuthErrorIfExists(EditScheduleActivity.this, e);
                            }

                            @Override
                            public void onNext(JobData data) {
                                Toast.makeText(EditScheduleActivity.this, R.string.sch_created, Toast.LENGTH_SHORT).show();
                                finish();
                            }
                        })
        );
    }

    private void hideLoading() {
        ProgressDialogFragment.dismiss(getSupportFragmentManager());
    }

    private void subscribe(Subscription subscription) {
        mCompositeSubscription.add(subscription);
    }

    private void requestJobInfo() {
        mRestClient.scheduleService()
                .flatMap(new Func1<RxReportScheduleService, Observable<JobForm>>() {
                    @Override
                    public Observable<JobForm> call(RxReportScheduleService rxReportScheduleService) {
                        return null;
                    }
                })
                .compose(RxTransformers.<JobForm>applySchedulers())
                .subscribe(new Subscriber<JobForm>() {
                    @Override
                    public void onCompleted() {
                        hideLoading();
                        analytics.sendEvent(Analytics.EventCategory.JOB.getValue(), Analytics.EventAction.VIEWED.getValue(), null);
                    }

                    @Override
                    public void onError(Throwable e) {
                        hideLoading();
                        RequestExceptionHandler.showAuthErrorIfExists(EditScheduleActivity.this, e);
                    }

                    @Override
                    public void onNext(JobForm data) {
                        mScheduleFragment = ScheduleFragment_
                                .builder()
                                .scheduleViewModel(JobConverter.toJobViewModel(data))
                                .build();

                        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction
                                .replace(R.id.content, mScheduleFragment)
                                .commit();
                    }
                });
    }
}
