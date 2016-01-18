package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */

/**
 * Simple View Holder for grid representation of resource item. View consist of icon and title.
 */
public class GridResourceViewHolder extends BaseResourceViewHolder {
    protected TopCropImageView ivIcon;
    protected TextView tvName;
    protected ImageButton btnInfo;

    public GridResourceViewHolder(View itemView) {
        super(itemView);

        this.ivIcon = (TopCropImageView) itemView.findViewById(android.R.id.icon);
        this.tvName = (TextView) itemView.findViewById(android.R.id.text1);
        this.btnInfo = (ImageButton) itemView.findViewById(R.id.showInfo);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemInteractionListener != null) {
                    mItemInteractionListener.onViewSingleClick(getAdapterPosition());
                }
            }
        });

        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemInteractionListener != null) {
                    mItemInteractionListener.onViewInfoClick(getAdapterPosition());
                }
            }
        });
    }

    @Override
    public void setTitle(CharSequence title) {
        tvName.setText(title);
    }

    @Override
    public void setSubTitle(CharSequence subTitle) {

    }

    @Override
    public TopCropImageView getImageView() {
        return ivIcon;
    }
}
