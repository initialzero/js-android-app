package com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters;

import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public abstract class FilterableAdapter<VH extends RecyclerView.ViewHolder, IT> extends RecyclerView.Adapter<VH> {

    private SortedList<Integer> mAppropriateItemsPositionList;
    private List<IT> mItemsList;
    private String mFilterWord;

    public FilterableAdapter(List<IT> valuesList) {
        if (valuesList == null) {
            throw new IllegalArgumentException("Items list can not be null");
        }
        this.mFilterWord = "";
        this.mItemsList = valuesList;
        this.mAppropriateItemsPositionList = new SortedList<>(Integer.class, new FilterableListSortCallback());
        updateAppropriateItemsList();
    }

    /**
     * Returns the total number of items in the data set hold by the adapter that match filter rule.
     *
     * @return The total number of items in this adapter that match filter rule.
     */
    @Override
    public final int getItemCount() {
        return mAppropriateItemsPositionList.size();
    }

    public final IT getItem(int position) {
        int positionInList = mAppropriateItemsPositionList.get(position);
        return mItemsList.get(positionInList);
    }

    public final int getItemPosition(int adapterPosition){
        return mAppropriateItemsPositionList.get(adapterPosition);
    }

    /**
     * Items list will be filtered with filter word.
     * For disabling filtering pass empty String as argument.
     * @param filterWord Word to filter with.
     */
    public void filter(String filterWord) {
        if (filterWord == null) {
            throw new IllegalArgumentException("Filter word can not be null");
        }
        mFilterWord = filterWord.toLowerCase();
        updateAppropriateItemsList();
    }

    /**
     * Update item state. Call this instead of {@link #notifyItemChanged(int) notifyItemChanged()}.
     * @param position Item position in list.
     */
    public void updateItem(int position){
        int adapterPosition = mAppropriateItemsPositionList.indexOf(position);
        notifyItemChanged(adapterPosition);
    }

    /**
     * Returns value that will be used to filter item list.
     *
     * @param item List item to query
     * @return value that will be used to filter item list
     */
    protected abstract String getValueForFiltering(IT item);

    private void updateAppropriateItemsList() {
        if (mFilterWord.isEmpty()) {
            initFullList();
            return;
        }

        mAppropriateItemsPositionList.beginBatchedUpdates();
        for (int i = 0; i < mItemsList.size(); i++) {
            String itemValue = getValueForFiltering(mItemsList.get(i)).toLowerCase();
            if (itemValue.contains(mFilterWord)) {
                mAppropriateItemsPositionList.add(i);
            } else {
                mAppropriateItemsPositionList.remove(i);
            }
        }
        mAppropriateItemsPositionList.endBatchedUpdates();
    }

    private void initFullList() {
        for (int i = 0; i < mItemsList.size(); i++) {
            mAppropriateItemsPositionList.add(i);
        }
    }

    private class FilterableListSortCallback extends SortedList.Callback<Integer> {
        @Override
        public int compare(Integer o1, Integer o2) {
            return o1.compareTo(o2);
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
        public boolean areContentsTheSame(Integer oldItem, Integer newItem) {
            return oldItem.equals(newItem);
        }

        @Override
        public boolean areItemsTheSame(Integer item1, Integer item2) {
            return item1.equals(item2);
        }
    }
}
