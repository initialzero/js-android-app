package com.jaspersoft.android.jaspermobile.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.support.FilterManagerBean;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EView;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
@EView
public class FilterTitleView extends Spinner {
    private static final int BY_REPORTS_POSITION = 1;
    private static final int BY_DASHBOARDS_POSITION = 2;

    @Bean
    FilterManagerBean filterManager;
    private ArrayList<String> mFilters;

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

    @AfterViews
    final void init() {
        initTitleView();
    }

    private void initTitleView() {
        setLayoutParams(new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

        CharSequence[] options = {
                getContext().getString(R.string.s_fd_option_all),
                getContext().getString(R.string.s_fd_option_reports),
                getContext().getString(R.string.s_fd_option_dashboards)
        };

        int position = 0;
        mFilters = filterManager.getFilters();

        if (filterManager.containsOnlyReport()) {
            position = BY_REPORTS_POSITION;
        }
        if (filterManager.containsOnlyDashboard()) {
            position = BY_DASHBOARDS_POSITION;
        }

        setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case BY_REPORTS_POSITION:
                        mFilters = filterManager.getReportFilters();
                        break;
                    case BY_DASHBOARDS_POSITION:
                        mFilters = filterManager.getDashboardFilters();
                        break;
                    default:
                        mFilters = filterManager.getFiltersForLibrary();
                        break;
                }
                filterManager.putFilters(mFilters);
                if (filterSelectedListener != null) {
                    filterSelectedListener.onDialogPositiveClick(mFilters);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        ArrayAdapter<CharSequence> filterAdapter = new ArrayAdapter<CharSequence>(getContext(), R.layout.item_library_filter,
                android.R.id.text1, options);
        filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        setAdapter(filterAdapter);
        setSelection(position);
    }

    public void setFilterSelectedListener(FilterDialogListener filterSelectedListener) {
        this.filterSelectedListener = filterSelectedListener;
    }

    public interface FilterDialogListener {
        void onDialogPositiveClick(List<String> types);
    }
}

