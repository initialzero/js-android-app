/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.support.v7.widget.AppCompatSpinner;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.filtering.Filter;
import com.jaspersoft.android.jaspermobile.util.filtering.ResourceFilter;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class FilterTitleView extends AppCompatSpinner {
    private boolean initialSelectionDone = false;

    private FilterListener filterSelectedListener;

    public FilterTitleView(Context context) {
        super(context);
    }

    public FilterTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * Init filter view with available filters
     *
     * @param resourceFilter provide available filters, and currently selected filter
     * @return true if view initialized successfully. False if there is only one available filters
     */
    public boolean init(final ResourceFilter resourceFilter) {
        setId(R.id.filter);

        if (resourceFilter.getFilters().size() == 1) {
            setVisibility(GONE);
            return false;
        }

        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        int position = resourceFilter.getPosition();

        setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (initialSelectionDone) {
                    Filter selectedFilter = resourceFilter.get(position);
                    if (filterSelectedListener != null) {
                        filterSelectedListener.onFilter(selectedFilter);
                    }
                }
                initialSelectionDone = true;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // It's a hack to make spinner width as a selected item width
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(getContext(), R.layout.item_library_filter, resourceFilter.getFilters()) {
            @Override
            public View getView(final int position, final View convertView,
                                final ViewGroup parent) {
                int selectedItemPosition = FilterTitleView.this.getSelectedItemPosition();
                return super.getView(selectedItemPosition, convertView, parent);
            }
        };
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setAdapter(filterAdapter);
        setSelection(position, false);

        return true;
    }

    public void setFilterSelectedListener(FilterListener filterSelectedListener) {
        this.filterSelectedListener = filterSelectedListener;
    }

    public interface FilterListener {
        void onFilter(Filter types);
    }
}

