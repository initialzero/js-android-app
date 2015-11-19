/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters;

import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subscriptions.Subscriptions;

/**
 * RecyclerView adapter that support item filtering.
 *
 * @author Andrew Tivodar
 * @since 2.2
 */
public abstract class FilterableAdapter<VH extends RecyclerView.ViewHolder, IT> extends RecyclerView.Adapter<VH> {

    private static final int LIMIT = 50;

    private int mOffset;
    private List<IT> mFilteredItemList;
    private List<IT> mItemsList;
    private String mFilterWord;
    private FilterListener mFilterListener;
    private Subscription filterSubscription;

    /**
     * Create adapter using provided data set.
     *
     * @param valuesList item data set. Can not be null.
     */
    public FilterableAdapter(List<IT> valuesList) {
        if (valuesList == null) {
            throw new IllegalArgumentException("Items list can not be null");
        }
        this.filterSubscription = Subscriptions.empty();
        this.mFilterWord = "";
        this.mItemsList = valuesList;
        this.mFilteredItemList = new ArrayList<>();
        loadNextItems();
    }

    public void setFilterListener(FilterListener filterListener) {
        this.mFilterListener = filterListener;
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
        mFilterWord = filterWord.toLowerCase();
        mOffset = 0;

        if (!filterSubscription.isUnsubscribed()) {
            filterSubscription.unsubscribe();
        }

        filterSubscription = Observable.defer(new Func0<Observable<List<IT>>>() {
            @Override
            public Observable<List<IT>> call() {
                return Observable.just(getNextFilteredItems());
            }
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<List<IT>>() {
                    @Override
                    public void call(List<IT> newItemList) {
                        animateList(newItemList);
                        filterSubscription = Subscriptions.empty();
                        mFilteredItemList = newItemList;
                        if (mFilterListener != null) {
                            mFilterListener.onFilterDone();
                        }
                    }
                });
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
        while (addedItem < LIMIT && mOffset < mItemsList.size()) {
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

    private void animateList(List<IT> newFilterList) {
        List<Integer> unRemovalList = new ArrayList<>();
        List<Integer> addList = new ArrayList<>();

        for (int i = 0; i < newFilterList.size(); i++) {
            IT item = newFilterList.get(i);
            int indexInPrevList = mFilteredItemList.indexOf(item);
            if (indexInPrevList == -1) {
                addList.add(i);
            } else {
                unRemovalList.add(indexInPrevList);
            }
        }

        int removedCount = 0;
        for (int i = 0; i < mFilteredItemList.size(); i++) {
            if (!unRemovalList.contains(i)) {
                notifyItemRemoved(i - removedCount);
                removedCount++;
            }
        }

        for (Integer index : addList) {
            notifyItemInserted(index);
        }
    }

    public interface FilterListener {
        void onFilterDone();
    }
}
