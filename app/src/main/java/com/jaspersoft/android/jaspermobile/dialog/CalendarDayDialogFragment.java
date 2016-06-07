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
public class CalendarDayDialogFragment extends BaseDialogFragment {

    private final static String SELECTED_DAYS_ARG = "SELECTED_DAYS_ARG";
    private final static String DAYS_ARG = "DAYS_ARG";

    private List<CalendarViewRecurrence.Day> selectedDays;
    private List<CalendarViewRecurrence.Day> days;
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
                    selectedDays.clear();
                    if (isChecked) {
                        selectedDays.addAll(days);
                    }
                } else {
                    int index = which - 1;
                    CalendarViewRecurrence.Day item = days.get(index);
                    if (isChecked) {
                        selectedDays.add(item);
                    } else {
                        selectedDays.remove(item);
                    }

                    items.get(which).checked = isChecked;
                    boolean allSelected = selectedDays.containsAll(days);
                    items.get(0).checked = allSelected;
                }
                adapter.notifyDataSetChanged();
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sr_days);
        builder.setView(listView);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (mDialogListener != null) {
                    ((DaysSelectedListener) mDialogListener).onDaysSelected(selectedDays);
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
            selectedDays = args.getParcelableArrayList(SELECTED_DAYS_ARG);
            days = args.getParcelableArrayList(DAYS_ARG);
        }
    }

    private List<CheckItem> createItems() {
        List<CheckItem> items = new ArrayList<>(days.size() + 1);

        CalendarViewRecurrence.Day allDays = CalendarViewRecurrence.Day.create(getString(R.string.s_fd_option_all), 90);
        CheckItem all = new CheckItem(allDays);
        all.checked = selectedDays.containsAll(days);
        items.add(all);

        for (CalendarViewRecurrence.Day day : days) {
            CheckItem item = new CheckItem(day);
            item.checked = selectedDays.contains(day);
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
        private final CalendarViewRecurrence.Day day;

        private CheckItem(CalendarViewRecurrence.Day day) {
            this.day = day;
        }

        @Override
        public String toString() {
            return day.toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CheckItem item = (CheckItem) o;

            return day != null ? day.equals(item.day) : item.day == null;
        }

        @Override
        public int hashCode() {
            return day != null ? day.hashCode() : 0;
        }
    }

    @Override
    protected Class<DaysSelectedListener> getDialogCallbackClass() {
        return DaysSelectedListener.class;
    }

    public static CalendarDayFragmentBuilder createBuilder(FragmentManager fragmentManager) {
        return new CalendarDayFragmentBuilder(fragmentManager);
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class CalendarDayFragmentBuilder extends BaseDialogFragmentBuilder<CalendarDayDialogFragment> {

        public CalendarDayFragmentBuilder(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public CalendarDayFragmentBuilder setSelected(List<CalendarViewRecurrence.Day> formats) {
            args.putParcelableArrayList(SELECTED_DAYS_ARG, new ArrayList<>(formats));
            return this;
        }

        public CalendarDayFragmentBuilder setDays(List<CalendarViewRecurrence.Day> formats) {
            args.putParcelableArrayList(DAYS_ARG, new ArrayList<>(formats));
            return this;
        }

        @Override
        protected CalendarDayDialogFragment build() {
            return new CalendarDayDialogFragment();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface DaysSelectedListener extends DialogClickListener {
        void onDaysSelected(List<CalendarViewRecurrence.Day> selectedDays);
    }
}
