package com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class MultiSelectSelectedAdapter extends RecyclerView.Adapter<MultiSelectSelectedAdapter.MultiSelectViewHolder> {

    private List<InputControlOption> mInputControlOptions;
    private List<OptionContainer> mSelectedList;
    private ItemSelectedListener mItemSelectListener;

    public MultiSelectSelectedAdapter(List<InputControlOption> inputControlOptions) {
        if (inputControlOptions == null) {
            throw new IllegalArgumentException("Input Controls Options list can not be null!");
        }
        this.mInputControlOptions = inputControlOptions;
        notifySelectionsChanged(mInputControlOptions);
    }

    @Override
    public MultiSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_multiselect_remove, parent, false);
        return new MultiSelectViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MultiSelectViewHolder viewHolder, int position) {
        InputControlOption item = mSelectedList.get(position).getOption();
        viewHolder.populateView(item);
    }

    @Override
    public int getItemCount() {
        return mSelectedList.size();
    }

    public void setItemSelectListener(ItemSelectedListener itemSelectListener) {
        this.mItemSelectListener = itemSelectListener;
    }

    public void notifySelectionChanged(boolean isSelected, int itemIndex) {
        int selectedIndex = getSortedIndex(itemIndex);
        if (isSelected) {
            mSelectedList.add(selectedIndex, new OptionContainer(mInputControlOptions.get(itemIndex), itemIndex));
            notifyItemInserted(selectedIndex);
        } else {
            mSelectedList.remove(selectedIndex - 1);
            notifyItemRemoved(selectedIndex - 1);
        }
    }

    public void notifySelectionsChanged(List<InputControlOption> inputControlOptions) {
        this.mSelectedList = new ArrayList<>();
        for (int i = 0; i < inputControlOptions.size(); i++) {
            if (inputControlOptions.get(i).isSelected()) {
                OptionContainer selectedContainer = new OptionContainer(inputControlOptions.get(i), i);
                mSelectedList.add(selectedContainer);
            }
        }
        notifyDataSetChanged();
    }

    private int getSortedIndex(int index){
        for (int i = 0; i < mSelectedList.size(); i++) {
            if (index < mSelectedList.get(i).getIndex()) return i;
        }
        return mSelectedList.size();
    }

    private class OptionContainer {
        private int index;
        private InputControlOption option;

        public OptionContainer(InputControlOption option, int index) {
            this.option = option;
            this.index = index;
        }

        public int getIndex() {
            return index;
        }

        public InputControlOption getOption() {
            return option;
        }
    }

    protected class MultiSelectViewHolder extends RecyclerView.ViewHolder {

        private TextView tvLabel;
        private ImageButton btnRemove;

        public MultiSelectViewHolder(View itemView) {
            super(itemView);
            tvLabel = (TextView) itemView.findViewById(R.id.tvMultiSelectLabel);
            btnRemove = (ImageButton) itemView.findViewById(R.id.btnRemoveMultiSelect);
            btnRemove.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedPosition = getAdapterPosition();
                    if (mItemSelectListener != null && selectedPosition > -1) {
                        int itemPosition = mSelectedList.get(selectedPosition).getIndex();
                        mItemSelectListener.onItemUnselected(itemPosition);
                    }
                }
            });
        }

        public void populateView(InputControlOption inputControlOption) {
            tvLabel.setText(inputControlOption.getLabel());
        }
    }

    public interface ItemSelectedListener {
        void onItemUnselected(int position);
    }
}