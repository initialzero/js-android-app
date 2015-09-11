package com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class SingleSelectIcAdapter extends FilterableAdapter<SingleSelectIcAdapter.SingleSelectViewHolder, InputControlOption> {

    private ItemSelectListener mItemSelectListener;

    public SingleSelectIcAdapter(List<InputControlOption> inputControlOptions) {
        super(inputControlOptions);
    }

    @Override
    public SingleSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_input_control_boolean, parent, false);
        return new SingleSelectViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(SingleSelectViewHolder viewHolder, int position) {
        viewHolder.populateView(getItem(position));
    }

    @Override
    protected String getValueForFiltering(InputControlOption item) {
        return item.getLabel();
    }

    public void setItemSelectListener(ItemSelectListener itemSelectListener) {
        this.mItemSelectListener = itemSelectListener;
    }

    protected class SingleSelectViewHolder extends RecyclerView.ViewHolder {

        private CheckBox cbSingleSelect;
        private TextView itemTitle;

        public SingleSelectViewHolder(View itemView) {
            super(itemView);
            cbSingleSelect = (CheckBox) itemView.findViewById(R.id.ic_boolean);
            itemTitle = (TextView) itemView.findViewById(R.id.ic_boolean_title);

            cbSingleSelect.setChecked(true);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemSelectListener != null) {
                        int itemPosition = getItemPosition(getAdapterPosition());
                        mItemSelectListener.onItemSelected(itemPosition);
                    }
                    cbSingleSelect.performClick();
                }
            });
        }

        public void populateView(InputControlOption inputControlOption) {
            itemTitle.setText(inputControlOption.getLabel());
            cbSingleSelect.setVisibility(inputControlOption.isSelected() ? View.VISIBLE : View.GONE);
            cbSingleSelect.setChecked(inputControlOption.isSelected());
        }
    }

    public interface ItemSelectListener {
        void onItemSelected(int position);
    }
}
