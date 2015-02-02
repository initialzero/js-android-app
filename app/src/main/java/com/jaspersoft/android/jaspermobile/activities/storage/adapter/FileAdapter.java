/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.storage.adapter;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.view.ActionMode;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.common.collect.Maps;
import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.activities.favorites.adapter.SingleChoiceSimpleCursorAdapter;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.GridItemView_;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.IResourceView;
import com.jaspersoft.android.jaspermobile.activities.repository.adapter.ListItemView_;
import com.jaspersoft.android.jaspermobile.activities.repository.support.ViewType;
import com.jaspersoft.android.jaspermobile.db.database.table.SavedItemsTable;
import com.jaspersoft.android.sdk.util.FileUtils;

import java.io.File;
import java.util.Map;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FileAdapter extends SingleChoiceSimpleCursorAdapter {

    private static final String[] FROM = {SavedItemsTable.NAME, SavedItemsTable.CREATION_TIME, SavedItemsTable.FILE_FORMAT};
    private static final int[] TO = {android.R.id.text1, android.R.id.text2, android.R.id.icon};

    public static enum FileType {
        HTML,
        PDF,
        XLS,
        UNKNOWN
    }

    private static final Map<FileType, Integer> DRAWABLE_IDS_MAP = Maps.newEnumMap(FileType.class);

    static {
        DRAWABLE_IDS_MAP.put(FileType.HTML, R.drawable.ic_composed_html);
        DRAWABLE_IDS_MAP.put(FileType.PDF, R.drawable.ic_composed_pdf);
        DRAWABLE_IDS_MAP.put(FileType.XLS, R.drawable.ic_composed_xls);
        DRAWABLE_IDS_MAP.put(FileType.UNKNOWN, R.drawable.js_grey_gradient);
    }


    private final ViewType mViewType;
    private FileInteractionListener fileInteractionListener;

    public FileAdapter(Context context, Bundle savedInstanceState, int layout, ViewType viewType) {
        super(savedInstanceState, context, layout, null, FROM, TO, 0);
        mViewType = viewType;
    }

    public void setFileInteractionListener(FileInteractionListener fileInteractionListener) {
        this.fileInteractionListener = fileInteractionListener;
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

        Cursor cursor = getCursor();
        cursor.moveToPosition(position);

        File file = new File(cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_PATH)));
        long creationTime = cursor.getLong(cursor.getColumnIndex(SavedItemsTable.CREATION_TIME));
        String fileFormat = cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_FORMAT));
        String fileName = cursor.getString(cursor.getColumnIndex(SavedItemsTable.NAME));

        itemView.getImageView().setImageResource(getFileIconByExtension(fileFormat));
        itemView.setTitle(fileName);
        itemView.setTimeStamp(getHumanReadableFileSize(file));
        itemView.setSubTitle(getFormattedDateModified(creationTime));

        return (View) itemView;
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
        Cursor cursor = getCursor();
        cursor.moveToPosition(getCurrentPosition());

        File file = new File(cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_PATH)));
        switch (item.getItemId()) {
            case R.id.renameItem:
                if (fileInteractionListener != null) {
                    String extension = cursor.getString(cursor.getColumnIndex(SavedItemsTable.FILE_FORMAT));
                    fileInteractionListener.onRename(file.getParentFile(), extension);
                }
                break;
            case R.id.deleteItem:
                if (fileInteractionListener != null) {
                    fileInteractionListener.onDelete(file.getParentFile());
                }
                break;
            case R.id.showAction:
                if (fileInteractionListener != null) {
                    String title = cursor.getString(cursor.getColumnIndex(SavedItemsTable.NAME));
                    long creationTime = cursor.getLong(cursor.getColumnIndex(SavedItemsTable.CREATION_TIME));
                    String description = String.format("%s \n %s", getFormattedDateModified(creationTime),
                            getHumanReadableFileSize(file));

                    fileInteractionListener.onInfo(title, description);
                }
                break;
            default:
                return false;
        }
        return true;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private int getFileIconByExtension(String extension) {
        FileType type = getFileTypeByExtension(extension);
        return DRAWABLE_IDS_MAP.get(type);
    }

    private FileType getFileTypeByExtension(String extension) {
        try {
            return FileType.valueOf(extension);
        } catch (IllegalArgumentException ex) {
            return FileType.UNKNOWN;
        }
    }

    private String getFormattedDateModified(long creationTime) {
        return DateUtils.formatDateTime(getContext(), creationTime,
                DateUtils.FORMAT_SHOW_DATE |
                        DateUtils.FORMAT_SHOW_TIME |
                        DateUtils.FORMAT_SHOW_YEAR |
                        DateUtils.FORMAT_NUMERIC_DATE |
                        DateUtils.FORMAT_24HOUR
        );
    }

    private String getHumanReadableFileSize(File file) {
        long byteSize = FileUtils.calculateFileSize(file.getParentFile());
        return FileUtils.getHumanReadableByteCount(byteSize);
    }

    //---------------------------------------------------------------------
    // Nested Classes
    //---------------------------------------------------------------------

    public static interface FileInteractionListener {
        void onRename(File file, String extention);

        void onDelete(File file);

        void onInfo(String title, String description);
    }

}
