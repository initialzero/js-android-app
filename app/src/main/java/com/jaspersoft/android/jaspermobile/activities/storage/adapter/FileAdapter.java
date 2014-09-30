package com.jaspersoft.android.jaspermobile.activities.storage.adapter;

import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Maps;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.GridItemView_;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.IResourceView;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ListItemView_;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.SingleChoiceArrayAdapter;
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
public class FileAdapter extends SingleChoiceArrayAdapter<File> {

    private final ViewType mViewType;
    private final Map<FileType, Integer> drawableIdsMap = Maps.newEnumMap(FileType.class);
    private FileInteractionListener fileInteractionListener;

    public static Builder builder(Context context, Bundle savedInstanceState) {
        checkNotNull(context);
        return new Builder(context, savedInstanceState);
    }

    private FileAdapter(Context context, Bundle savedInstanceState, ViewType viewType) {
        super(savedInstanceState, context, 0);
        mViewType = checkNotNull(viewType, "ViewType can`t be null");

        drawableIdsMap.put(FileType.HTML, R.drawable.ic_composed_html);
        drawableIdsMap.put(FileType.PDF, R.drawable.ic_composed_pdf);
        drawableIdsMap.put(FileType.XLS, R.drawable.ic_composed_xls);
        drawableIdsMap.put(FileType.UNKNOWN, R.drawable.js_grey_gradient);
    }

    @Override
    public View getViewImpl(int position, View convertView, ViewGroup parent) {
        IResourceView itemView = (IResourceView) convertView;

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

    public void setFileInteractionListener(FileInteractionListener fileInteractionListener) {
        this.fileInteractionListener = fileInteractionListener;
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

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.am_saved_items_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        File file = getItem(getCurrentPosition());
        switch (item.getItemId()) {
            case R.id.openItem:
                if (fileInteractionListener != null) {
                    fileInteractionListener.onOpened(file);
                }
                break;
            case R.id.renameItem:
                if (fileInteractionListener != null) {
                    fileInteractionListener.onRenamed(file);
                }
                break;
            case R.id.deleteItem:
                if (fileInteractionListener != null) {
                    fileInteractionListener.onDelete(getCurrentPosition(), file);
                }
                break;
            default:
                return false;
        }
        return true;
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

    private static enum FileType {
        HTML,
        PDF,
        XLS,
        UNKNOWN
    }

    public static interface FileInteractionListener {
        void onOpened(File item);
        void onRenamed(File file);
        void onDelete(int currentPosition, File file);
    }

    public static class Builder {
        private final Context context;
        private final Bundle savedInstanceState;

        private ViewType viewType;

        public Builder(Context context, Bundle savedInstanceState) {
            this.context = context;
            this.savedInstanceState = savedInstanceState;
        }

        public Builder setViewType(ViewType viewType) {
            this.viewType = viewType;
            return this;
        }

        public FileAdapter create() {
            return new FileAdapter(context, savedInstanceState, viewType);
        }
    }
}
