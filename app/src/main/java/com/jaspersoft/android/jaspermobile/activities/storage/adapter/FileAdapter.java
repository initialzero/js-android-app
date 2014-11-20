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
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
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

import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class FileAdapter extends SingleChoiceArrayAdapter<File> {

    private final ViewType mViewType;
    private static final Map<FileType, Integer> DRAWABLE_IDS_MAP = Maps.newEnumMap(FileType.class);
    static {
        DRAWABLE_IDS_MAP.put(FileType.HTML, R.drawable.ic_composed_html);
        DRAWABLE_IDS_MAP.put(FileType.PDF, R.drawable.ic_composed_pdf);
        DRAWABLE_IDS_MAP.put(FileType.XLS, R.drawable.ic_composed_xls);
        DRAWABLE_IDS_MAP.put(FileType.UNKNOWN, R.drawable.js_grey_gradient);
    }
    private FileInteractionListener fileInteractionListener;

    public static Builder builder(Context context, Bundle savedInstanceState) {
        checkNotNull(context);
        return new Builder(context, savedInstanceState);
    }

    private FileAdapter(Context context, Bundle savedInstanceState, ViewType viewType) {
        super(savedInstanceState, context, 0);
        mViewType = checkNotNull(viewType, "ViewType can`t be null");
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
        itemView.getImageView().setImageResource(getFileIconByExtension(extension));

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
        return DRAWABLE_IDS_MAP.get(type);
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
            case R.id.renameItem:
                if (fileInteractionListener != null) {
                    fileInteractionListener.onRename(file);
                }
                break;
            case R.id.deleteItem:
                if (fileInteractionListener != null) {
                    fileInteractionListener.onDelete(getCurrentPosition(), file);
                }
                break;
            case R.id.showAction:
                showInfo(file);
                break;
            default:
                return false;
        }
        return true;
    }

    private void showInfo(File file) {
        String title = FileUtils.getBaseName(file.getName());
        String description = String.format("%s \n %s", getFormattedDateModified(file),
                getHumanReadableFileSize(file));

        FragmentManager fm = ((FragmentActivity) getContext()).getSupportFragmentManager();
        SimpleDialogFragment.createBuilder(getContext(), fm)
                .setTitle(title)
                .setMessage(description)
                .show();
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
        void onRename(File file);
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
