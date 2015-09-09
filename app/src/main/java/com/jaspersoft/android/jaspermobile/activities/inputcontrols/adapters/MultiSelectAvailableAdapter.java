package com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class MultiSelectAvailableAdapter extends RecyclerView.Adapter<MultiSelectAvailableAdapter.MultiSelectViewHolder> {

    private List<InputControlOption> mInputControlOptions;
    private ItemSelectListener mItemSelectListener;

    public MultiSelectAvailableAdapter(List<InputControlOption> inputControlOptions) {
        if (inputControlOptions == null) {
            throw new IllegalArgumentException("Input Controls Options list can not be null!");
        }
        this.mInputControlOptions = inputControlOptions;
    }

    @Override
    public MultiSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_input_control_boolean, parent, false);
        return new MultiSelectViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MultiSelectViewHolder viewHolder, int position) {
        viewHolder.populateView(mInputControlOptions.get(position));
    }

    @Override
    public int getItemCount() {
        return mInputControlOptions.size();
    }

    public void setItemSelectListener(ItemSelectListener itemSelectListener) {
        this.mItemSelectListener = itemSelectListener;
    }

    protected class MultiSelectViewHolder extends RecyclerView.ViewHolder {

        private CheckBox cbSingleSelect;

        public MultiSelectViewHolder(View itemView) {
            super(itemView);
            cbSingleSelect = (CheckBox) itemView;
            cbSingleSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemSelectListener != null) {
                        mItemSelectListener.onItemSelected(getAdapterPosition());
                    }
                }
            });
        }

        public void populateView(InputControlOption inputControlOption) {
            cbSingleSelect.setText(inputControlOption.getLabel());
            cbSingleSelect.setChecked(inputControlOption.isSelected());
        }
    }

    public interface ItemSelectListener {
        void onItemSelected(int position);
    }
}