/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.ui.entity.job.CalendarViewRecurrence;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class CalendarMonthDialogFragment extends BaseDialogFragment {

    private final static String SELECTED_MONTHS_ARG = "SELECTED_MONTHS_ARG";
    private final static String MONTHS_ARG = "MONTHS_ARG";

    private List<CalendarViewRecurrence.Month> selectedMonths;
    private List<CalendarViewRecurrence.Month> months;
    private int mMultiChoiceItemLayout;
    private int mListLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final TypedArray a = getActivity().obtainStyledAttributes(null, android.support.v7.appcompat.R.styleable.AlertDialog,
                android.support.v7.appcompat.R.attr.alertDialogStyle, 0);

        mMultiChoiceItemLayout = a.getResourceId(android.support.v7.appcompat.R.styleable.AlertDialog_multiChoiceItemLayout, 0);
        mListLayout = a.getResourceId(android.support.v7.appcompat.R.styleable.AlertDialog_listLayout, 0);

        a.recycle();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        ListView listView = (ListView) inflater.inflate(mListLayout, null);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        final List<CheckItem> items = createItems();
        final Adapter adapter = new Adapter(getActivity(), listView, mMultiChoiceItemLayout, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int which, long id) {
                ListView listView = (ListView) parent;
                boolean isChecked = listView.isItemChecked(which);
                if (which == 0) {
                    for (CheckItem item : items) {
                        item.checked = isChecked;
                    }
                    selectedMonths.clear();
                    if (isChecked) {
                        selectedMonths.addAll(months);
                    }
                } else {
                    int index = which - 1;
                    CalendarViewRecurrence.Month item = months.get(index);
                    if (isChecked) {
                        selectedMonths.add(item);
                    } else {
                        selectedMonths.remove(item);
                    }

                    items.get(which).checked = isChecked;
                    boolean allSelected = selectedMonths.containsAll(months);
                    items.get(0).checked = allSelected;
                }
                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sr_months);
        builder.setView(listView);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDialogListener != null) {
                    ((MonthsSelectedListener) mDialogListener).onMonthsSelected(selectedMonths);
                }
            }
        });

        builder.setNegativeButton(R.string.cancel, null);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    protected void initDialogParams() {
        super.initDialogParams();

        Bundle args = getArguments();
        if (args != null) {
            selectedMonths = args.getParcelableArrayList(SELECTED_MONTHS_ARG);
            months = args.getParcelableArrayList(MONTHS_ARG);
        }
    }

    private List<CheckItem> createItems() {
        List<CheckItem> items = new ArrayList<>(months.size() + 1);

        CalendarViewRecurrence.Month allMonth = CalendarViewRecurrence.Month.create(getString(R.string.s_fd_option_all), 90);
        CheckItem all = new CheckItem(allMonth);
        all.checked = selectedMonths.containsAll(months);
        items.add(all);

        for (CalendarViewRecurrence.Month day : months) {
            CheckItem item = new CheckItem(day);
            item.checked = selectedMonths.contains(day);
            items.add(item);
        }

        return items;
    }

    private static class Adapter extends ArrayAdapter<CheckItem> {
        private List<CheckItem> items;
        private ListView listView;

        public Adapter(Context context, ListView listView, int layout, List<CheckItem> items) {
            super(context, layout, android.R.id.text1, items);
            this.items = items;
            this.listView = listView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            if (items != null) {
                CheckItem checkItem = items.get(position);
                boolean isItemChecked = checkItem.checked;
                listView.setItemChecked(position, isItemChecked);
            }
            return view;
        }
    }

    private final class CheckItem {
        private boolean checked;
        private final CalendarViewRecurrence.Month month;

        private CheckItem(CalendarViewRecurrence.Month month) {
            this.month = month;
        }

        @Override
        public String toString() {
            return month.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CheckItem item = (CheckItem) o;

            return month != null ? month.equals(item.month) : item.month == null;
        }

        @Override
        public int hashCode() {
            return month != null ? month.hashCode() : 0;
        }
    }

    @Override
    protected Class<MonthsSelectedListener> getDialogCallbackClass() {
        return MonthsSelectedListener.class;
    }

    public static CalendarMonthFragmentBuilder createBuilder(FragmentManager fragmentManager) {
        return new CalendarMonthFragmentBuilder(fragmentManager);
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class CalendarMonthFragmentBuilder extends BaseDialogFragmentBuilder<CalendarMonthDialogFragment> {

        public CalendarMonthFragmentBuilder(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public CalendarMonthFragmentBuilder setSelected(List<CalendarViewRecurrence.Month> formats) {
            args.putParcelableArrayList(SELECTED_MONTHS_ARG, new ArrayList<>(formats));
            return this;
        }

        public CalendarMonthFragmentBuilder setMonths(List<CalendarViewRecurrence.Month> formats) {
            args.putParcelableArrayList(MONTHS_ARG, new ArrayList<>(formats));
            return this;
        }

        @Override
        protected CalendarMonthDialogFragment build() {
            return new CalendarMonthDialogFragment();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface MonthsSelectedListener extends DialogClickListener {
        void onMonthsSelected(List<CalendarViewRecurrence.Month> selectedMonths);
    }
}
