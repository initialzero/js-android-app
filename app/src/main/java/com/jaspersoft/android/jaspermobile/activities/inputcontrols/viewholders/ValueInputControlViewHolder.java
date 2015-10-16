package com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders;

import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public abstract class ValueInputControlViewHolder extends BaseInputControlViewHolder {

    private View item;
    private TextView label;
    private TextView value;
    private TextView errorText;

    private ClickListener mClickListener;

    public ValueInputControlViewHolder(View itemView) {
        super(itemView);
        value = (TextView) itemView.findViewById(R.id.ic_value);
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
    public void populateView(InputControl inputControl) {
        value.setEnabled(!inputControl.isReadOnly());
        item.setEnabled(!inputControl.isReadOnly());

        value.setText(getCurrentValue(inputControl));
        label.setText(getUpdatedLabelText(inputControl));

        showError(errorText, inputControl);
    }

    public void setOnSelectListener(ClickListener onSelectListener) {
        this.mClickListener = onSelectListener;
    }

    protected abstract String getCurrentValue(InputControl inputControl);

    public interface ClickListener {
        void onClick(int position);
    }
}
