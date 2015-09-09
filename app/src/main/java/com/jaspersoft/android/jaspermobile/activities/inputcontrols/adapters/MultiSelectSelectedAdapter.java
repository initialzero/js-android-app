package com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters;

import android.support.v7.util.SortedList;
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
public class MultiSelectSelectedAdapter extends RecyclerView.Adapter<MultiSelectSelectedAdapter.MultiSelectViewHolder> {

    private List<InputControlOption> mInputControlOptions;
    private SortedList<Integer> mSelectedList;
    private ItemSelectedListener mItemSelectListener;

    public MultiSelectSelectedAdapter(List<InputControlOption> inputControlOptions) {
        if (inputControlOptions == null) {
            throw new IllegalArgumentException("Input Controls Options list can not be null!");
        }
        this.mInputControlOptions = inputControlOptions;
        initSelectedList();
    }

    @Override
    public MultiSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_input_control_boolean, parent, false);
        return new MultiSelectViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MultiSelectViewHolder viewHolder, int position) {
        int itemPosition = mSelectedList.get(position);
        viewHolder.populateView(mInputControlOptions.get(itemPosition));
    }

    @Override
    public long getItemId(int position) {
        return mSelectedList.get(position);
    }

    @Override
    public int getItemCount() {
        return mSelectedList.size();
    }

    public void setItemSelectListener(ItemSelectedListener itemSelectListener) {
        this.mItemSelectListener = itemSelectListener;
    }

    public void selectItem(boolean isSelected, int itemIndex) {
        if (isSelected) {
            mSelectedList.add(itemIndex);
        } else {
            mSelectedList.remove(itemIndex);
        }
    }

    private void initSelectedList() {
        mSelectedList = new SortedList<>(Integer.class, new SelectedListSortCallback());
        for (int i = 0; i < mInputControlOptions.size(); i++) {
            if (mInputControlOptions.get(i).isSelected()) {
                mSelectedList.add(i);
            }
        }
    }

    private class SelectedListSortCallback extends SortedList.Callback<Integer>{
        @Override
        public int compare(Integer integer, Integer t21) {
            return integer.compareTo(t21);
        }

        @Override
        public void onInserted(int position, int count) {
            notifyItemRangeInserted(position, count);
        }

        @Override
        public void onRemoved(int position, int count) {
            notifyItemRangeRemoved(position, count);
        }

        @Override
        public void onMoved(int fromPosition, int toPosition) {
            notifyItemMoved(fromPosition, toPosition);
        }

        @Override
        public void onChanged(int position, int count) {
            notifyItemRangeChanged(position, count);
        }

        @Override
        public boolean areContentsTheSame(Integer integer, Integer t21) {
            return integer.equals(t21);
        }

        @Override
        public boolean areItemsTheSame(Integer integer, Integer t21) {
            return integer.equals(t21);
        }
    }

    protected class MultiSelectViewHolder extends RecyclerView.ViewHolder {

        private CheckBox cbSingleSelect;

        public MultiSelectViewHolder(View itemView) {
            super(itemView);
            cbSingleSelect = (CheckBox) itemView;
            cbSingleSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int selectedPosition = getAdapterPosition();
                    if (mItemSelectListener != null && selectedPosition > -1) {
                        int itemPosition = (int) MultiSelectSelectedAdapter.this.getItemId(selectedPosition);
                        mItemSelectListener.onItemUnselected(itemPosition);
                    }
                }
            });
        }

        public void populateView(InputControlOption inputControlOption) {
            cbSingleSelect.setText(inputControlOption.getLabel());
            cbSingleSelect.setChecked(inputControlOption.isSelected());
        }
    }

    public interface ItemSelectedListener {
        void onItemUnselected(int position);
    }
}