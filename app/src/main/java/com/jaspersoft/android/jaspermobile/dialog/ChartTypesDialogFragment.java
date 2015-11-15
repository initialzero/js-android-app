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

package com.jaspersoft.android.jaspermobile.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class ChartTypesDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener{

    private LayoutInflater lInflater;
    private List<String> mChartTypes;
    private List<Integer> mChartTypesIcons;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        lInflater = (LayoutInflater) getActivity()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mChartTypes = new ArrayList<String>() {{
            add("Bar");
            add("Column");
            add("Line");
            add("Area");
            add("Spline");
            add("Pie");
            add("SpiderLine");
        }};

        mChartTypesIcons = new ArrayList<Integer>() {{
            add(R.drawable.ic_chart_bar);
            add(R.drawable.ic_chart_column);
            add(R.drawable.ic_chart_line);
            add(R.drawable.ic_chart_area);
            add(R.drawable.ic_chart_spline);
            add(R.drawable.ic_chart_pie);
            add(R.drawable.ic_chart_spider_line);
        }};

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Select chart type...");

        builder.setSingleChoiceItems(new ListAdapter() {
            @Override
            public boolean areAllItemsEnabled() {
                return true;
            }

            @Override
            public boolean isEnabled(int position) {
                return true;
            }

            @Override
            public void registerDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public void unregisterDataSetObserver(DataSetObserver observer) {

            }

            @Override
            public int getCount() {
                return mChartTypes.size();
            }

            @Override
            public String getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public boolean hasStableIds() {
                return false;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = convertView;
                if (view == null) {
                    view = lInflater.inflate(R.layout.item_chart_type, parent, false);
                }

                ((TextView) view.findViewById(android.R.id.text1)).setText(mChartTypes.get(position));
                ((ImageView) view.findViewById(android.R.id.icon)).setImageResource(mChartTypesIcons.get(position));

                return view;
            }
                                        
            @Override
            public int getItemViewType(int position) {
                return 0;
            }

            @Override
            public int getViewTypeCount() {
                return 1;
            }

            @Override
            public boolean isEmpty() {
                return false;
            }
        },
                -1, this);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mDialogListener != null) {
            ((ChartTypeDialogClickListener) mDialogListener).onChartTypeSelected(mChartTypes.get(which));
        }
        dismiss();
    }

    @Override
    protected Class<ChartTypeDialogClickListener> getDialogCallbackClass() {
        return ChartTypeDialogClickListener.class;
    }

    public static ChartTypeDialogFragmentBuilder createBuilder(FragmentManager fragmentManager) {
        return new ChartTypeDialogFragmentBuilder(fragmentManager);
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class ChartTypeDialogFragmentBuilder extends BaseDialogFragmentBuilder<ChartTypesDialogFragment> {

        public ChartTypeDialogFragmentBuilder(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        @Override
        protected ChartTypesDialogFragment build() {
            return new ChartTypesDialogFragment();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface ChartTypeDialogClickListener extends DialogClickListener {
        void onChartTypeSelected(String chartType);
    }

}
