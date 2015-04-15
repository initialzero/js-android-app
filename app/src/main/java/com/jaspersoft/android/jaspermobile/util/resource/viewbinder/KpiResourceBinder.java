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

package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ResourceView;
import com.jaspersoft.android.retrofit.sdk.account.AccountServerData;
import com.jaspersoft.android.retrofit.sdk.account.JasperAccountManager;
import com.jaspersoft.android.retrofit.sdk.ojm.Kpi;
import com.jaspersoft.android.retrofit.sdk.rest.service.KpiModule;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class KpiResourceBinder extends ReportResourceBinder {
    public KpiResourceBinder(Context context) {
        super(context);
    }

    @Override
    public void bindView(final ResourceView resourceView, ResourceAdapter.KpiResourceLookup lookup) {
        ResourceLookup item = lookup.getResource();
        setIcon(resourceView.getImageView(), item.getUri());
        resourceView.getTitleView().setText(item.getLabel());

        if (resourceView.getKpiImage() != null) {
            Kpi cachedKpi = lookup.getKpiCache();

            if (cachedKpi == null) {
                if (resourceView.getSubTitleView() != null) {
                    resourceView.getSubTitleView().setText(item.getDescription());
                }
                loadKpi(resourceView, lookup);
            } else {
                applyKpi(resourceView, cachedKpi);
            }
        }
    }

    private void loadKpi(final ResourceView resourceView, final ResourceAdapter.KpiResourceLookup lookup) {
        JasperAccountManager accountManager = JasperAccountManager.get(getContext());
        final AccountServerData serverData = AccountServerData.get(getContext(), accountManager.getActiveAccount());

        accountManager.getNonBlockingActiveAuthToken()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String cookie) {
                        String endpoint = serverData.getServerUrl() + JasperSettings.DEFAULT_REST_VERSION;
                        RestAdapter restAdapter = new RestAdapter.Builder().setEndpoint(endpoint).build();
                        KpiModule module = restAdapter.create(KpiModule.class);

                        module.getKpi(cookie,
                                String.format("%s%s", lookup.getResource().getUri(), "_files/KPI.json"),
                                new Callback<Collection<Kpi>>() {
                                    @Override
                                    public void success(Collection<Kpi> kpiResponse, Response response) {
                                        Kpi kpi = new ArrayList<Kpi>(kpiResponse).get(0);
                                        lookup.setKpiCache(kpi);
                                        applyKpi(resourceView, kpi);
                                    }

                                    @Override
                                    public void failure(RetrofitError error) {
                                        Timber.e(error, error.getMessage());
                                    }
                                });
                    }
                });
    }

    private void applyKpi(ResourceView resourceView, Kpi kpi) {
        float fromTarget, resultTarget = 0f;
        if (kpi.getTarget() != 0) {
            fromTarget = Float.valueOf(kpi.getValue()) * 100 / Float.valueOf(kpi.getTarget());
            resultTarget = fromTarget - 100f;
        }

        String color;
        ImageView kpiImage = resourceView.getKpiImage();
        kpiImage.setVisibility(View.VISIBLE);
        if (resultTarget > 0) {
            color = "green";
            kpiImage.setImageResource(R.drawable.ic_kpi_up);
        } else {
            color = "red";
            kpiImage.setImageResource(R.drawable.ic_kpi_down);
        }

        StringBuilder resultHtml = new StringBuilder()
                .append("<span>")
                .append("<font color=\"")
                .append(color).append("\">")
                .append(String.format("%.2f", resultTarget))
                .append("%</font>")
                .append("<font style=\"color: grey\">");

        int count = resourceView.getMiscView() == null ? 30 : 10;
        for (int i = 0; i < count; i++) {
            resultHtml.append("&nbsp;");
        }
        resultHtml
                .append(getFormattedValue(kpi))
                .append("</font>")
                .append("</span>")
                .toString();

        if (resourceView.getMiscView() == null) {
            TextView subTextView = resourceView.getSubTitleView();
            if (subTextView != null) {
                subTextView.setText("");
                subTextView.setText(Html.fromHtml(resultHtml.toString()));
            }
        } else {
            TextView miscView = resourceView.getMiscView();
            miscView.setVisibility(View.VISIBLE);
            miscView.setText(Html.fromHtml(resultHtml.toString()));
        }
    }

    private static String getFormattedValue(Kpi kpi) {
        String pattern = kpi.getValuePattern();
        String value = String.valueOf(kpi.getValue());
        if (!TextUtils.isEmpty(pattern)) {
            DecimalFormat decimalFormat = (DecimalFormat) DecimalFormat.getInstance();
            decimalFormat.applyPattern(pattern);
            try {
                return String.valueOf(decimalFormat.parse(value));
            } catch (ParseException e) {
                Timber.w(e, e.getMessage());
            }
        }
        return String.valueOf(value);
    }

}
