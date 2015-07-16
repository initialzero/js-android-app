package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */

/**
 * Simple View Holder for grid representation of resource item. View consist of icon and title.
 */
public class SimpleGridViewHolder extends BaseViewHolder {
    protected TopCropImageView ivIcon;
    protected TextView tvName;

    public SimpleGridViewHolder(View itemView) {
        super(itemView);

        this.ivIcon = (TopCropImageView) itemView.findViewById(android.R.id.icon);
        this.tvName = (TextView) itemView.findViewById(android.R.id.text1);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemInteractionListener != null) {
                    mItemInteractionListener.onViewSingleClick(getAdapterPosition());
                }
            }
        });

        itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mItemInteractionListener != null) {
                    mItemInteractionListener.onViewLongClick(getAdapterPosition());
                    return true;
                }
                return false;
            }
        });
    }

    /**
     * Fill resource view with data. This method does not contain setting of item icon.
     * @param resource data to be displayed in UI
     */
    @Override
    public void populateView(JasperResource resource) {
        tvName.setText(resource.getLabel());
    }
}
