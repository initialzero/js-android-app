package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.SavedItemResource;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.EnumMap;
import java.util.Map;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class SavedItemViewBinder extends ResourceViewBinder {

    private static final Map<SavedItemResource.FileType, Integer> DRAWABLE_IDS_MAP = new EnumMap<SavedItemResource.FileType, Integer>(SavedItemResource.FileType.class);

    static {
        DRAWABLE_IDS_MAP.put(SavedItemResource.FileType.HTML, R.drawable.bg_saved_html);
        DRAWABLE_IDS_MAP.put(SavedItemResource.FileType.PDF, R.drawable.bg_saved_pdf);
        DRAWABLE_IDS_MAP.put(SavedItemResource.FileType.XLS, R.drawable.bg_saved_xls);
        DRAWABLE_IDS_MAP.put(SavedItemResource.FileType.UNKNOWN, R.drawable.bg_gradient_grey);
    }

    public SavedItemViewBinder(Context context, ViewType mViewType) {
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

    private void setThumbnail(ImageView imageView, SavedItemResource.FileType extension){
        ((TopCropImageView) imageView).setScaleType(TopCropImageView.ScaleType.FIT_CENTER);
        imageView.setBackgroundResource(R.drawable.bg_gradient_grey);

        int iconRes = DRAWABLE_IDS_MAP.get(extension);
        ImageLoader.getInstance().displayImage("", imageView, getDisplayImageOptions(iconRes));
    }

    private DisplayImageOptions getDisplayImageOptions(int iconRes) {
        return new DisplayImageOptions.Builder()
                .showImageForEmptyUri(iconRes)
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
            if (!(resource instanceof SavedItemResource)) return;

            SavedItemResource savedItemResource = ((SavedItemResource) resource);
            setThumbnail(ivIcon, savedItemResource.getFileType());
        }
    }

    public class GridFolderViewHolder extends SimpleGridViewHolder {

        public GridFolderViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void populateView(JasperResource resource, boolean isSelected) {
            super.populateView(resource, isSelected);
            if (!(resource instanceof SavedItemResource)) return;

            SavedItemResource savedItemResource = ((SavedItemResource) resource);
            setThumbnail(ivIcon, savedItemResource.getFileType());
        }
    }
}
