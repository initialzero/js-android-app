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

package com.jaspersoft.android.jaspermobile.activities.inputcontrols.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControlOption;

import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
public class SingleSelectIcAdapter extends FilterableAdapter<SingleSelectIcAdapter.SingleSelectViewHolder, InputControlOption> {

    private ItemSelectListener mItemSelectListener;

    public SingleSelectIcAdapter(List<InputControlOption> inputControlOptions) {
        super(inputControlOptions);
    }

    @Override
    public SingleSelectViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.item_input_control_boolean, parent, false);
        return new SingleSelectViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(SingleSelectViewHolder viewHolder, int position) {
        viewHolder.populateView(getItem(position));
    }

    @Override
    protected String getValueForFiltering(InputControlOption item) {
        return item.getLabel();
    }

    public void setItemSelectListener(ItemSelectListener itemSelectListener) {
        this.mItemSelectListener = itemSelectListener;
    }

    protected class SingleSelectViewHolder extends RecyclerView.ViewHolder {

        private CheckBox cbSingleSelect;
        private TextView itemTitle;

        public SingleSelectViewHolder(View itemView) {
            super(itemView);
            cbSingleSelect = (CheckBox) itemView.findViewById(R.id.ic_boolean);
            itemTitle = (TextView) itemView.findViewById(R.id.ic_boolean_title);

            cbSingleSelect.setChecked(true);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemSelectListener != null) {
                        int itemPosition = getItemPosition(getAdapterPosition());
                        mItemSelectListener.onItemSelected(itemPosition);
                    }
                    cbSingleSelect.performClick();
                }
            });
        }

        public void populateView(InputControlOption inputControlOption) {
            itemTitle.setText(inputControlOption.getLabel());
            cbSingleSelect.setVisibility(inputControlOption.isSelected() ? View.VISIBLE : View.GONE);
            cbSingleSelect.setChecked(inputControlOption.isSelected());
        }
    }

    public interface ItemSelectListener {
        void onItemSelected(int position);
    }
}
