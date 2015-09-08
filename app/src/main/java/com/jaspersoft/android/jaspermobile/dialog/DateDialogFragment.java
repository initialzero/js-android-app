package com.jaspersoft.android.jaspermobile.dialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.widget.DatePicker;
import android.widget.TimePicker;

import org.androidannotations.annotations.EFragment;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EFragment
public class DateDialogFragment extends BaseDialogFragment {

    public final static int DATE = 0;
    public final static int TIME = 1;

    private final static String IC_ID_ARG = "ic_id_arg";
    private final static String DATE_ARG = "date_arg";
    private final static String TYPE_ARG = "type_arg";

    private int type;
    protected String icId;
    protected Calendar activeDate;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog;
        if (type == DATE) {
            dialog = new DatePickerDialog(getActivity(), new DateChangeListener(), activeDate.get(Calendar.YEAR), activeDate.get(Calendar.MONTH), activeDate.get(Calendar.DAY_OF_MONTH));
        } else {
            return new TimePickerDialog(getActivity(), new TimeChangeListener(), activeDate.get(Calendar.HOUR_OF_DAY), activeDate.get(Calendar.MINUTE), true);
        }
        return dialog;
    }

    @Override
    protected Class<DateDialogClickListener> getDialogCallbackClass() {
        return DateDialogClickListener.class;
    }

    protected void initDialogParams() {
        super.initDialogParams();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(DATE_ARG)) {
                long dateMs = args.getLong(DATE_ARG);
                activeDate = Calendar.getInstance();
                activeDate.setTimeInMillis(dateMs);
            }
            if (args.containsKey(IC_ID_ARG)) {
                icId = args.getString(IC_ID_ARG);
            }
            if (args.containsKey(TYPE_ARG)) {
                type = args.getInt(TYPE_ARG);
            }
        }
    }

    public static DateDialogFragmentBuilder createBuilder(FragmentManager fragmentManager) {
        return new DateDialogFragmentBuilder(fragmentManager);
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class DateDialogFragmentBuilder extends BaseDialogFragmentBuilder<DateDialogFragment> {

        public DateDialogFragmentBuilder(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public DateDialogFragmentBuilder setInputControlId(String id) {
            args.putString(IC_ID_ARG, id);
            return this;
        }

        public DateDialogFragmentBuilder setDate(Calendar date) {
            long dateLong = date != null ? date.getTimeInMillis() : new Date().getTime();
            args.putLong(DATE_ARG, dateLong);
            return this;
        }

        public DateDialogFragmentBuilder setType(int type) {
            args.putInt(TYPE_ARG, type);
            return this;
        }

        @Override
        protected DateDialogFragment build() {
            return new DateDialogFragment_();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface DateDialogClickListener extends DialogClickListener {
        void onDateSelected(String id, Calendar date);
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    private class DateChangeListener implements DatePickerDialog.OnDateSetListener {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            activeDate.set(year, monthOfYear, dayOfMonth);
            ((DateDialogClickListener) mDialogListener).onDateSelected(icId, activeDate);
        }
    }

    private class TimeChangeListener implements TimePickerDialog.OnTimeSetListener {

        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            activeDate.set(Calendar.HOUR_OF_DAY, hourOfDay);
            activeDate.set(Calendar.MINUTE, minute);
            ((DateDialogClickListener) mDialogListener).onDateSelected(icId, activeDate);
        }
    }
}
