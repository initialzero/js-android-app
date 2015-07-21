package com.jaspersoft.android.jaspermobile.util.resource.viewbinder;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.ViewType;
import com.jaspersoft.android.jaspermobile.util.resource.JasperResource;
import com.jaspersoft.android.jaspermobile.util.resource.ReportResource;
import com.jaspersoft.android.jaspermobile.widget.TopCropImageView;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class ReportViewBinder extends ResourceViewBinder {

    public ReportViewBinder(Context context, ViewType mViewType) {
        super(context, mViewType);
    }

    @Override
    public BaseViewHolder provideListViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_resource_list, parent, false);
        return new ListReportViewHolder(itemView);
    }

    @Override
    public BaseViewHolder provideGridViewHolder(ViewGroup parent) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.item_resource_grid, parent, false);
        return new GridReportViewHolder(itemView);
    }

    //---------------------------------------------------------------------
    // Helper Methods
    //---------------------------------------------------------------------

    private void setThumbnail(ImageView imageView, String uri){
        imageView.setBackgroundResource(R.drawable.bg_gradient_grey);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        loadFromNetwork(imageView, uri);
    }

    private void loadFromNetwork(ImageView imageView, String uri) {
        ImageLoader.getInstance().displayImage(
                uri, imageView, getDisplayImageOptions(),
                new ThumbnailLoadingListener()
        );
    }

    private DisplayImageOptions getDisplayImageOptions() {
        return new DisplayImageOptions.Builder()
                .showImageOnLoading(R.drawable.placeholder_report)
                .showImageForEmptyUri(R.drawable.placeholder_report)
                .considerExifParams(true)
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    private static class ThumbnailLoadingListener extends SimpleImageLoadingListener {
        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (view != null) {
                ((TopCropImageView) view).setScaleType(TopCropImageView.ScaleType.MATRIX);
                ((TopCropImageView) view).setScaleType(TopCropImageView.ScaleType.TOP_CROP);
            }
        }
    }

    //---------------------------------------------------------------------
    // Nested ViewHolders
    //---------------------------------------------------------------------

    public class ListReportViewHolder extends SimpleListViewHolder {

        public ListReportViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void populateView(JasperResource resource, boolean isSelected) {
            super.populateView(resource, isSelected);
            if (!(resource instanceof ReportResource)) return;

            ReportResource reportResource = ((ReportResource) resource);
            setThumbnail(ivIcon, reportResource.getThumbnailUri());
        }
    }

    public class GridReportViewHolder extends SimpleGridViewHolder {

        public GridReportViewHolder(View itemView) {
            super(itemView);
        }

        @Override
        public void populateView(JasperResource resource, boolean isSelected) {
            super.populateView(resource, isSelected);
            if (!(resource instanceof ReportResource)) return;

            ReportResource reportResource = ((ReportResource) resource);
            setThumbnail(ivIcon, reportResource.getThumbnailUri());
        }
    }
}
