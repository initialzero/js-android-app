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
public class MultiSelectAvailableAdapter extends FilterableAdapter<MultiSelectAvailableAdapter.MultiSelectViewHolder, InputControlOption> {

    private ItemSelectListener mItemSelectListener;

    public MultiSelectAvailableAdapter(List<InputControlOption> inputControlOptions) {
        super(inputControlOptions);
    }

    @Override
    public MultiSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_input_control_boolean, parent, false);
        return new MultiSelectViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MultiSelectViewHolder viewHolder, int position) {
        viewHolder.populateView(getItem(position));
    }

    @Override
    protected String getValueForFiltering(InputControlOption item) {
        return item.getLabel();
    }

    public void setItemSelectListener(ItemSelectListener itemSelectListener) {
        this.mItemSelectListener = itemSelectListener;
    }

    protected class MultiSelectViewHolder extends RecyclerView.ViewHolder {

        private CheckBox cbSelect;
        private TextView itemTitle;

        public MultiSelectViewHolder(View itemView) {
            super(itemView);

            cbSelect = (CheckBox) itemView.findViewById(R.id.ic_boolean);
            itemTitle = (TextView) itemView.findViewById(R.id.ic_boolean_title);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemSelectListener != null) {
                        int filteredPosition = getItemPosition(getAdapterPosition());
                        mItemSelectListener.onItemSelected(filteredPosition);
                    }
                    cbSelect.performClick();
                }
            });
        }

        public void populateView(InputControlOption inputControlOption) {
            itemTitle.setText(inputControlOption.getLabel());
            cbSelect.setChecked(inputControlOption.isSelected());
        }
    }

    public interface ItemSelectListener {
        void onItemSelected(int position);
    }
}