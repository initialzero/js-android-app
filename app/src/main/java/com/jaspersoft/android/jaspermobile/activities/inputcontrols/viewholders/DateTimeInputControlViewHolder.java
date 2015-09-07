package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.validation.DateTimeFormatValidationRule;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import timber.log.Timber;

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
    private DateFormat mServerDateFormat;
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
                    mDateTimeClickListener.onDateClick(getPosition());
                }
            }
        });

        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateTimeClickListener != null) {
                    mDateTimeClickListener.onTimeClick(getPosition());
                }
            }
        });

        btnClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDateTimeClickListener != null) {
                    mDateTimeClickListener.onClear(getPosition());
                }
            }
        });

        selectedDateTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setClearButtonVisibility(s.equals(InputControlWrapper.NOTHING_SUBSTITUTE_LABEL));
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public void populateView(InputControl inputControl, boolean enabled) {
        enableViews(inputControl, enabled);

        mServerDateFormat = parseServerDateFormat(inputControl);
        String selectedDate = getSelectedDate(inputControl);
        selectedDateTime.setText(selectedDate != null ? selectedDate : InputControlWrapper.NOTHING_SUBSTITUTE_LABEL);
        label.setText(getUpdatedLabelText(inputControl));

        showError(errorText, inputControl);
    }

    public void setDateTimeClickListener(DateTimeClickListener dateTimeClickListener) {
        this.mDateTimeClickListener = dateTimeClickListener;
    }

    private DateFormat parseServerDateFormat(InputControl inputControl) {
        DateFormat dateFormat = mUserDateFormat;

        for (DateTimeFormatValidationRule validationRule : inputControl.getValidationRules(DateTimeFormatValidationRule.class)) {
            String serverDateFormat = validationRule.getFormat();
            dateFormat = new SimpleDateFormat(serverDateFormat, Locale.US);
        }
        return dateFormat;
    }

    private String getSelectedDate(InputControl inputControl) {
        String selectedValue = inputControl.getState().getValue();
        if (selectedValue != null) {
            try {
                Date date = mServerDateFormat.parse(selectedValue);
                return mUserDateFormat.format(date);
            } catch (ParseException e) {
                Timber.e("Can not parse date: %s", selectedValue);
            }
        }
        return null;
    }

    private void setClearButtonVisibility(boolean visible) {
        btnClear.setVisibility(visible ? View.VISIBLE : View.GONE);
        clearDivider.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void enableViews(InputControl inputControl, boolean enabled) {
        selectedDateTime.setEnabled(enabled && !inputControl.isReadOnly());
        btnDate.setEnabled(enabled && !inputControl.isReadOnly());
        btnTime.setEnabled(enabled && !inputControl.isReadOnly());
        btnClear.setEnabled(enabled && !inputControl.isReadOnly());
    }

    public interface DateTimeClickListener {
        void onDateClick(int position);

        void onTimeClick(int position);

        void onClear(int position);
    }
}
