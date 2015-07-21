package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class FolderViewBinder extends ResourceViewBinder {

    public FolderViewBinder(Context context, ViewType mViewType) {
        super(context, mViewType);
    }

    @Override
    public BaseViewHolder provideListViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_resource_list, parent, false);
        return new ListFolderViewHolder(itemView);
    }

    @Override
    public BaseViewHolder provideGridViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_resource_grid, parent, false);
        return new GridFolderViewHolder(itemView);
    }

    //---------------------------------------------------------------------
    // Helper Methods
    //---------------------------------------------------------------------

    private void setThumbnail(ImageView imageView){
        ((TopCropImageView) imageView).setScaleType(TopCropImageView.ScaleType.FIT_XY);
        imageView.setBackgroundResource(R.drawable.bg_gradient_blue);
        ImageLoader.getInstance().displayImage("", imageView, getDisplayImageOptions());
    }

    private DisplayImageOptions getDisplayImageOptions() {
        return new DisplayImageOptions.Builder()
                .showImageForEmptyUri(R.drawable.placeholder_folder)
                .build();
    }

    //---------------------------------------------------------------------
    // Nested ViewHolders
    //---------------------------------------------------------------------

    public class ListFolderViewHolder extends SimpleListViewHolder {

        public ListFolderViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void populateView(JasperResource resource, boolean isSelected) {
            super.populateView(resource, isSelected);
            setThumbnail(ivIcon);
        }
    }

    public class GridFolderViewHolder extends SimpleGridViewHolder {

        public GridFolderViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void populateView(JasperResource resource, boolean isSelected) {
            super.populateView(resource, isSelected);
            setThumbnail(ivIcon);
        }
    }
}
