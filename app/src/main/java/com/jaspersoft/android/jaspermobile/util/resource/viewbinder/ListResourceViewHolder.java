package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class ListResourceViewHolder extends BaseResourceViewHolder {
    protected ImageView ivIcon;
    protected TextView tvName;
    protected TextView tvDescription;
    protected ImageButton btnSecondaryAction;

    public ListResourceViewHolder(View itemView) {
        super(itemView);

        this.ivIcon = (ImageView) itemView.findViewById(android.R.id.icon);
        this.tvName = (TextView) itemView.findViewById(android.R.id.text1);
        this.tvDescription = (TextView) itemView.findViewById(android.R.id.text2);
        this.btnSecondaryAction = (ImageButton) itemView.findViewById(R.id.secondaryAction);

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemInteractionListener != null) {
                    mItemInteractionListener.onViewSingleClick(getAdapterPosition());
                }
            }
        });

        btnSecondaryAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mItemInteractionListener != null) {
                    mItemInteractionListener.onSecondaryActionClick(getAdapterPosition());
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
        tvDescription.setText(subTitle);
    }

    @Override
    public ImageView getImageView() {
        return ivIcon;
    }

    @Override
    public boolean isImageThumbnail() {
        return false;
    }

    @Override
    public void setSecondaryAction(int actionImage) {
        btnSecondaryAction.setImageResource(actionImage);
    }
}
