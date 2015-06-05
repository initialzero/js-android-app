package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.filtering.Filter;
import com.jaspersoft.android.jaspermobile.util.filtering.FilterStorage;
import com.jaspersoft.android.jaspermobile.util.filtering.ResourceFilter;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class FilterTitleView extends Spinner {
    private boolean initialSelectionDone = false;

    private FilterDialogListener filterSelectedListener;

    public FilterTitleView(Context context) {
        super(context);
    }

    public FilterTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public FilterTitleView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init(final ResourceFilter resourceFilter, FilterStorage filterStorage) {
        setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        int position = resourceFilter.indexOf(filterStorage.getFilter());

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
        ArrayAdapter<String> filterAdapter = new ArrayAdapter<String>(getContext(), R.layout.item_library_filter, resourceFilter.getAvailableFilters()) {
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
    }

    public void setFilterSelectedListener(FilterDialogListener filterSelectedListener) {
        this.filterSelectedListener = filterSelectedListener;
    }

    public interface FilterDialogListener {
        void onFilter(Filter types);
    }
}

