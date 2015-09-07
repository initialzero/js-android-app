package com.jaspersoft.android.jaspermobile.activities.inputcontrols;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.BaseInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.BooleanInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.DateInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.DateTimeInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.NumberValueInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.SelectInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.TimeInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.ValueInputControlViewHolder;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import java.util.ArrayList;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class InputControlsAdapter extends RecyclerView.Adapter<BaseInputControlViewHolder> {

    private final static int IC_UNSUPPORTED = -1;
    private final static int IC_BOOLEAN = 0;
    private final static int IC_VALUE = 1;
    private final static int IC_NUMBER_VALUE = 2;
    private final static int IC_DATE_TIME = 3;
    private final static int IC_DATE = 4;
    private final static int IC_TIME = 5;
    private final static int IC_SINGLE_SELECT = 6;
    private final static int IC_MULTI_SELECT = 7;

    private ArrayList<InputControl> mInputControls;
    private boolean mEnabled;
    private LayoutInflater mLayoutInflater;

    public InputControlsAdapter(ArrayList<InputControl> inputControls) {
        if (inputControls == null) {
            throw new IllegalArgumentException("Input Controls can not be null!");
        }

        this.mInputControls = inputControls;
        this.mEnabled = true;
    }

    public void updateInputControlList(ArrayList<InputControl> inputControls) {
        this.mInputControls = inputControls;
    }

    public void setListEnabled(boolean enabled) {
        mEnabled = enabled;
        notifyItemRangeChanged(0, mInputControls.size());
    }

    @Override
    public BaseInputControlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem;
        switch (viewType) {
            case IC_BOOLEAN:
                listItem = layoutInflater.inflate(R.layout.item_input_control_boolean, parent, false);
                return new BooleanInputControlViewHolder(listItem);
            case IC_VALUE:
                listItem = layoutInflater.inflate(R.layout.item_input_control_value, parent, false);
                return new ValueInputControlViewHolder(listItem);
            case IC_NUMBER_VALUE:
                listItem = layoutInflater.inflate(R.layout.item_input_control_value, parent, false);
                return new NumberValueInputControlViewHolder(listItem);
            case IC_DATE_TIME:
                listItem = layoutInflater.inflate(R.layout.item_input_control_date, parent, false);
                return new DateTimeInputControlViewHolder(listItem);
            case IC_DATE:
                listItem = layoutInflater.inflate(R.layout.item_input_control_date, parent, false);
                return new DateInputControlViewHolder(listItem);
            case IC_TIME:
                listItem = layoutInflater.inflate(R.layout.item_input_control_date, parent, false);
                return new TimeInputControlViewHolder(listItem);
            case IC_SINGLE_SELECT:
                listItem = layoutInflater.inflate(R.layout.item_input_control_select, parent, false);
                return new SelectInputControlViewHolder(listItem);
            case IC_MULTI_SELECT:
                listItem = layoutInflater.inflate(R.layout.item_input_control_select, parent, false);
                return new SelectInputControlViewHolder(listItem);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(BaseInputControlViewHolder viewHolder, int position) {
        viewHolder.populateView(mInputControls.get(position), mEnabled);
    }

    @Override
    public int getItemCount() {
        return mInputControls.size();
    }

    @Override
    public int getItemViewType(int position) {
        switch (mInputControls.get(position).getType()) {
            case bool:
                return IC_BOOLEAN;
            case singleValueText:
                return IC_VALUE;
            case singleValueNumber:
                return IC_NUMBER_VALUE;
            case singleValueTime:
                return IC_TIME;
            case singleValueDate:
                return IC_DATE;
            case singleValueDatetime:
                return IC_DATE_TIME;
            case singleSelect:
            case singleSelectRadio:
                return IC_SINGLE_SELECT;
            case multiSelect:
            case multiSelectCheckbox:
                return IC_MULTI_SELECT;
            default:
                return IC_UNSUPPORTED;
        }
    }
}
