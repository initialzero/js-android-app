package com.jaspersoft.android.jaspermobile.activities.storage.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.google.common.collect.Maps;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.GridItemView_;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.IResourceView;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ListItemView_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.sdk.util.FileUtils;

import java.io.File;
import java.util.Comparator;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FileAdapter extends ArrayAdapter<File> {

    private final ViewType mViewType;
    private final Map<FileType, Integer> drawableIdsMap = Maps.newEnumMap(FileType.class);

    public static Builder builder(Context context) {
        checkNotNull(context);
        return new Builder(context);
    }

    public FileAdapter(Context context, ViewType viewType) {
        super(context, 0);
        mViewType = checkNotNull(viewType, "ViewType can`t be null");

        drawableIdsMap.put(FileType.HTML, com.jaspersoft.android.sdk.ui.R.drawable.ic_type_html);
        drawableIdsMap.put(FileType.PDF, com.jaspersoft.android.sdk.ui.R.drawable.ic_type_pdf);
        drawableIdsMap.put(FileType.XLS, com.jaspersoft.android.sdk.ui.R.drawable.ic_type_xls);
        drawableIdsMap.put(FileType.UNKNOWN, com.jaspersoft.android.sdk.ui.R.drawable.ic_type_unknown);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getViewImpl(position, (IResourceView) convertView);
    }

    private View getViewImpl(int position, IResourceView convertView) {
        IResourceView itemView;
        if (mViewType == ViewType.LIST) {
            itemView = convertView;
        } else {
            itemView = convertView;
        }

        if (itemView == null) {
            if (mViewType == ViewType.LIST) {
                itemView = ListItemView_.build(getContext());
            } else {
                itemView = GridItemView_.build(getContext());
            }
        }

        File file = getItem(position);
        String extension = FileUtils.getExtension(file.getName());
        itemView.setImageIcon(getFileIconByExtension(extension));

        itemView.setTitle(FileUtils.getBaseName(file.getName()));
        itemView.setTimeStamp(getHumanReadableFileSize(file));
        itemView.setSubTitle(getFormattedDateModified(file));

        return (View) itemView;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private int getFileIconByExtension(String extension) {
        FileType type = getFileTypeByExtension(extension);
        return drawableIdsMap.get(type);
    }

    private FileType getFileTypeByExtension(String extension) {
        try {
            return FileType.valueOf(extension);
        } catch (IllegalArgumentException ex) {
            return FileType.UNKNOWN;
        }
    }

    private String getFormattedDateModified(File file) {
        return DateUtils.formatDateTime(getContext(), file.lastModified(),
                DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_TIME |
                        DateUtils.FORMAT_SHOW_YEAR |
                        DateUtils.FORMAT_NUMERIC_DATE |
                        DateUtils.FORMAT_24HOUR
        );
    }

    private String getHumanReadableFileSize(File file) {
        long bytes = FileUtils.calculateFileSize(file);
        return FileUtils.getHumanReadableByteCount(bytes);
    }

    public void sortByLstModified() {
        sort(new LastModifiedComparator());
    }

    private static class LastModifiedComparator implements Comparator<File> {
        @Override
        public int compare(File f1, File f2) {
            return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
        }
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    public enum FileType {
        HTML,
        PDF,
        XLS,
        UNKNOWN
    }

    public static class Builder {
        private final Context context;

        private ViewType viewType;

        public Builder(Context context) {
            this.context = context;
        }

        public Builder setViewType(ViewType viewType) {
            this.viewType = viewType;
            return this;
        }

        public FileAdapter create() {
            return new FileAdapter(context, viewType);
        }
    }
}
