package com.jaspersoft.android.jaspermobile.activities.inputcontrols;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.BaseInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.BooleanInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.DateInputControlViewHolder;
import com.jaspersoft.android.jaspermobile.activities.inputcontrols.viewholders.SelectInputControlViewHolder;
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
    private final static int IC_DATE = 2;
    private final static int IC_SELECT = 3;

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
        if (viewType == IC_BOOLEAN) {
            View booleanInuitControlView = layoutInflater.inflate(R.layout.item_input_control_boolean, parent, false);
            return new BooleanInputControlViewHolder(booleanInuitControlView);
        } else if (viewType == IC_VALUE) {
            View ValueInuitControlView = layoutInflater.inflate(R.layout.item_input_control_value, parent, false);
            return new ValueInputControlViewHolder(ValueInuitControlView);
        } else if (viewType == IC_SELECT) {
            View selectInuitControlView = layoutInflater.inflate(R.layout.item_input_control_select, parent, false);
            return new SelectInputControlViewHolder(selectInuitControlView);
        } else if (viewType == IC_DATE) {
            View dateInuitControlView = layoutInflater.inflate(R.layout.item_input_control_date, parent, false);
            return new DateInputControlViewHolder(dateInuitControlView);
        }
        return null;
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
            case singleValueNumber:
                return IC_VALUE;
            case singleValueTime:
            case singleValueDate:
            case singleValueDatetime:
                return IC_DATE;
            case singleSelect:
            case singleSelectRadio:
            case multiSelect:
            case multiSelectCheckbox:
                return IC_SELECT;
            default:
                return IC_UNSUPPORTED;
        }
    }
}
