package com.jaspersoft.android.jaspermobile.activities.inputcontrols;

import android.app.Activity;
import android.content.Intent;
import android.support.design.widget.TabLayout;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters.MultiSelectAvailableAdapter;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters.MultiSelectSelectedAdapter;
import com.jaspersoft.android.jaspermobile.activities.robospice.RoboToolbarActivity;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EActivity(R.layout.activity_multi_select)
public class MultiSelectActivity extends RoboToolbarActivity {

    public static final String SELECT_IC_ARG = "select_input_control_id";

    @Inject
    protected ReportParamsStorage paramsStorage;

    @Extra
    protected String reportUri;

    @Extra
    protected String inputControlId;

    @ViewById(R.id.tlMultiSelect)
    protected TabLayout headerTab;

    private List<InputControlOption> mInputControlOptions;
    private String mInputControlLabel;

    @AfterViews
    protected void init() {
        initInputControlOptions();
        initViews();

        getSupportActionBar().setTitle(mInputControlLabel);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onBackPressed() {
        Intent dataIntent = new Intent();
        dataIntent.putExtra(SELECT_IC_ARG, inputControlId);
        setResult(Activity.RESULT_OK, dataIntent);

        super.onBackPressed();
    }

    private void initViews() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.vpMultiSelect);
        MultiSelectViewPagerAdapter adapter = new MultiSelectViewPagerAdapter();
        viewPager.setAdapter(adapter);

        headerTab.setupWithViewPager(viewPager);
    }

    private void initInputControlOptions() {
        ArrayList<InputControl> inputControls = paramsStorage.getInputControls(reportUri);
        for (InputControl inputControl : inputControls) {
            if (inputControl.getId().equals(inputControlId)) {
                mInputControlLabel = inputControl.getLabel();
                mInputControlOptions = inputControl.getState().getOptions();
                break;
            }
        }
    }

    private class MultiSelectViewPagerAdapter extends PagerAdapter {
        private final static int TAB_COUNT = 2;
        private final static int TAB_AVAILABLE = 0;
        private final static int TAB_SELECTED = 1;

        private MultiSelectAvailableAdapter mAvailableAdapter;
        private MultiSelectSelectedAdapter mSelectedAdapter;

        public MultiSelectViewPagerAdapter() {
            mAvailableAdapter = new MultiSelectAvailableAdapter(mInputControlOptions);
            mSelectedAdapter = new MultiSelectSelectedAdapter(mInputControlOptions);

            mAvailableAdapter.setItemSelectListener(new MultiSelectAvailableAdapter.ItemSelectListener() {
                @Override
                public void onItemSelected(int position) {
                    onItemSelectionChanged(position, !mInputControlOptions.get(position).isSelected());
                }
            });
            mSelectedAdapter.setItemSelectListener(new MultiSelectSelectedAdapter.ItemSelectedListener() {
                @Override
                public void onItemUnselected(int position) {
                    onItemSelectionChanged(position, false);
                }
            });
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view.equals(o);
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            LayoutInflater layoutInflater = LayoutInflater.from(MultiSelectActivity.this);
            RecyclerView list = (RecyclerView) layoutInflater.inflate(R.layout.report_options_layout, container, false);
            list.setLayoutManager(new LinearLayoutManager(MultiSelectActivity.this));

            if (position == TAB_AVAILABLE) {
                list.setAdapter(mAvailableAdapter);
            } else {
                list.setAdapter(mSelectedAdapter);
            }

            container.addView(list, 0);
            return list;

        }

        @Override
        public void destroyItem(ViewGroup collection, int position, Object view) {
            collection.removeView(((View) view));
        }

        @Override
        public CharSequence getPageTitle(int position) {
            if (position == TAB_AVAILABLE) {
                return getString(R.string.ro_ms_available, mAvailableAdapter.getItemCount());
            }
            return getString(R.string.ro_ms_selected, mSelectedAdapter.getItemCount());
        }

        private void onItemSelectionChanged(int position, boolean isSelected) {
            InputControlOption inputControlOption = mInputControlOptions.get(position);
            inputControlOption.setSelected(isSelected);

            mAvailableAdapter.notifyItemChanged(position);
            mSelectedAdapter.selectItem(isSelected, position);

            updateTabTitle();
        }

        private void updateTabTitle(){
            TabLayout.Tab selectedTab = headerTab.getTabAt(TAB_SELECTED);
            if (selectedTab != null) {
                selectedTab.setText(getString(R.string.ro_ms_selected, mSelectedAdapter.getItemCount()));
            }
        }
    }
}
