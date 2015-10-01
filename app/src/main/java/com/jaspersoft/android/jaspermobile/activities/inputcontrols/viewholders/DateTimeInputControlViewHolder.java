package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.IcDateHelper;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class DateTimeInputControlViewHolder extends BaseInputControlViewHolder {

    private static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private TextView label;
    private TextView selectedDateTime;
    private TextView errorText;
    protected ImageButton btnDate;
    protected ImageButton btnTime;
    private ImageButton btnClear;
    private View clearDivider;
    protected View dateTimeDivider;

    protected SimpleDateFormat mUserDateFormat;
    private DateTimeClickListener mDateTimeClickListener;

    public DateTimeInputControlViewHolder(View itemView) {
        super(itemView);

        mUserDateFormat = new SimpleDateFormat(DEFAULT_DATE_TIME_FORMAT, Locale.getDefault());

        selectedDateTime = (TextView) itemView.findViewById(R.id.ic_datetime_text);
        errorText = (TextView) itemView.findViewById(R.id.ic_error_text);
        label = (TextView) itemView.findViewById(R.id.ic_text_label);
        btnDate = (ImageButton) itemView.findViewById(R.id.ic_date);
        btnTime = (ImageButton) itemView.findViewById(R.id.ic_time);
        btnClear = (ImageButton) itemView.findViewById(R.id.ic_clear);
        clearDivider = itemView.findViewById(R.id.ic_clear_divider);
        dateTimeDivider = itemView.findViewById(R.id.ic_datetime_divider);

        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateTimeClickListener != null) {
                    mDateTimeClickListener.onDateClick(getAdapterPosition());
                }
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateTimeClickListener != null) {
                    mDateTimeClickListener.onTimeClick(getAdapterPosition());
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateTimeClickListener != null) {
                    mDateTimeClickListener.onClear(getAdapterPosition());
                }
            }
        });

        selectedDateTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setClearButtonVisibility(!s.toString().equals(InputControlWrapper.NOTHING_SUBSTITUTE_LABEL));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void populateView(InputControl inputControl) {
        String selectedDate = getSelectedDate(inputControl);
        selectedDateTime.setText(selectedDate != null ? selectedDate : InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);
        label.setText(getUpdatedLabelText(inputControl));

        showError(errorText, inputControl);
    }

    public void setDateTimeClickListener(DateTimeClickListener dateTimeClickListener) {
        this.mDateTimeClickListener = dateTimeClickListener;
    }

    private String getSelectedDate(InputControl inputControl) {
        Calendar calendarDate = IcDateHelper.convertToDate(inputControl);

        if (calendarDate != null) {
            return mUserDateFormat.format(calendarDate.getTime());
        }
        return null;
    }

    private void setClearButtonVisibility(boolean visible) {
        btnClear.setVisibility(visible ? View.VISIBLE : View.GONE);
        clearDivider.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    public interface DateTimeClickListener {
        void onDateClick(int position);

        void onTimeClick(int position);

        void onClear(int position);
    }
}
