package com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter that support item filtering.
 *
 * @author Andrew Tivodar
 * @since 2.2
 */
public abstract class FilterableAdapter<VH extends RecyclerView.ViewHolder, IT> extends RecyclerView.Adapter<VH> {

    private static final int LIMIT = 30;

    private int mOffset;
    private List<IT> mFilteredItemList;
    private List<IT> mItemsList;
    private String mFilterWord;

    /**
     * Create adapter using provided data set.
     *
     * @param valuesList item data set. Can not be null.
     */
    public FilterableAdapter(List<IT> valuesList) {
        if (valuesList == null) {
            throw new IllegalArgumentException("Items list can not be null");
        }
        this.mFilterWord = "";
        this.mItemsList = valuesList;
        this.mFilteredItemList = new ArrayList<>();
        loadNextItems();
    }

    /**
     * Returns the total number of items in the data set hold by the adapter that match filter rule.
     *
     * @return The total number of items in this adapter that match filter rule.
     */
    @Override
    public final int getItemCount() {
        return mFilteredItemList.size();
    }

    /**
     * Returns item from the data set by index in filtered data set.
     *
     * @param position Adapter index - index in filtered data set, not in full one.
     * @return item from data set
     */
    public final IT getItem(int position) {
        return mFilteredItemList.get(position);
    }

    /**
     * Convert filtered position to position in full data set.
     *
     * @param adapterPosition Position in filtered data set.
     * @return Position in full data set.
     */
    public final int getItemPosition(int adapterPosition) {
        return mItemsList.indexOf(mFilteredItemList.get(adapterPosition));
    }

    /**
     * Update item state. Call this instead of {@link #notifyItemChanged(int) notifyItemChanged()}.
     *
     * @param position Item position in data set.
     */
    public void updateItem(int position) {
        int filteredIndex = mFilteredItemList.indexOf(mItemsList.get(position));
        if (filteredIndex != -1) {
            notifyItemChanged(filteredIndex);
        }
    }

    /**
     * Filters data set with filter word.
     * For disabling filtering pass empty {@link String String} as argument.
     *
     * @param filterWord Word to filter with. Can not be null.
     */
    public void filter(final String filterWord) {
        if (filterWord == null) {
            throw new IllegalArgumentException("Filter word can not be null");
        }
        String oldFilterWord = mFilterWord;
        mFilterWord = filterWord.toLowerCase();

        List<IT> oldFilteredList = new ArrayList<>();
        oldFilteredList.addAll(mFilteredItemList);
        
        mOffset = 0;
        mFilteredItemList = new ArrayList<>();
        mFilteredItemList.addAll(getNextFilteredItems());

        animateList(oldFilteredList, oldFilterWord);
    }

    /**
     * Returns value that will be used to filter item list.
     *
     * @param item List item to query
     * @return value that will be used to filter item list
     */
    protected abstract String getValueForFiltering(IT item);

    public void loadNextItems() {
        int previousSize = mFilteredItemList.size();
        mFilteredItemList.addAll(getNextFilteredItems());
        int newSize = mFilteredItemList.size();
        notifyItemRangeInserted(previousSize, newSize - previousSize);
    }

    private List<IT> getNextFilteredItems() {
        List<IT> additionalList = new ArrayList<>();
        int addedItem = 0;
        while (addedItem < LIMIT || mOffset >= mItemsList.size() - 1) {
            IT item = mItemsList.get(mOffset);
            String valueForFiltering = getValueForFiltering(item).toLowerCase();
            boolean valueContainsFilterWord = valueForFiltering.contains(mFilterWord);

            if (valueContainsFilterWord) {
                additionalList.add(item);
                addedItem++;
            }
            mOffset++;
        }
        return additionalList;
    }

    private void animateList(List<IT> previousFilterList, String oldFilterWord) {
        int alreadyAdded = 0;
        int alreadyRemoved = 0;

        int animCount = Math.min(LIMIT, mItemsList.size());
        
        if (previousFilterList.size() > animCount) {
            notifyItemRangeRemoved(animCount, previousFilterList.size() - animCount);
        }

        for (int i = 0; i < animCount; i++) {
            IT item = mItemsList.get(i);
            String valueForFiltering = getValueForFiltering(item).toLowerCase();
            boolean valueContainsNewFilterWord = valueForFiltering.contains(mFilterWord);
            boolean valueContainsOldFilterWord = valueForFiltering.contains(oldFilterWord);
            if (valueContainsNewFilterWord) {
                if (!valueContainsOldFilterWord) {
                    notifyItemInserted(alreadyAdded);
                }
                alreadyAdded++;
            } else if (valueContainsOldFilterWord) {
                int indexToAnim = previousFilterList.indexOf(item);
                notifyItemRemoved(indexToAnim - alreadyRemoved);
                alreadyRemoved++;
            }
        }
    }
}
