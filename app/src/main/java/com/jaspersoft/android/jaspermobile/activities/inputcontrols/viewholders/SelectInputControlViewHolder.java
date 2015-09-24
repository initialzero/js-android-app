package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.ic.InputControlWrapper;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class SelectInputControlViewHolder extends BaseInputControlViewHolder {

    private View item;
    private TextView label;
    private TextView selectedValue;
    private TextView errorText;

    private ClickListener mClickListener;

    public SelectInputControlViewHolder(View itemView) {
        super(itemView);
        selectedValue = (TextView) itemView.findViewById(R.id.ic_selected_value);
        errorText = (TextView) itemView.findViewById(R.id.ic_error_text);
        label = (TextView) itemView.findViewById(R.id.ic_text_label);
        item = itemView;

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mClickListener != null) {
                    mClickListener.onClick(getAdapterPosition());
                }
            }
        });
    }

    @Override
    public void populateView(InputControl inputControl, boolean enabled) {
        selectedValue.setEnabled(enabled && !inputControl.isReadOnly());
        item.setEnabled(enabled && !inputControl.isReadOnly());
        selectedValue.setText(getCurrentSelection(inputControl));
        label.setText(getUpdatedLabelText(inputControl));

        showError(errorText, inputControl);
    }

    public void setOnSelectListener(ClickListener onSelectListener) {
        this.mClickListener = onSelectListener;
    }

    protected String getCurrentSelection(InputControl inputControl) {
        for (InputControlOption option : inputControl.getState().getOptions()) {
            if (option.isSelected()) {
                return option.getLabel();
            }
        }
        return InputControlWrapper.NOTHING_SUBSTITUTE_LABEL;
    }

    public interface ClickListener {
        void onClick(int position);
    }
}
