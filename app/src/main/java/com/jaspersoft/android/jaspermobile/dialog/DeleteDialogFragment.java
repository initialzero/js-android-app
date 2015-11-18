/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.dialog;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.FragmentManager;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class DeleteDialogFragment extends SimpleDialogFragment {

    private final static String RECORD_URI_ARG = "record_uri";
    private final static String FILE_URI_ARG = "file_uri";
    private List<String> recordsUri;
    private List<File> itemsFile;

    @Override
    protected Class<DeleteDialogClickListener> getDialogCallbackClass() {
        return DeleteDialogClickListener.class;
    }

    @Override
    protected void initDialogParams() {
        super.initDialogParams();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(RECORD_URI_ARG)) {
                recordsUri = args.getStringArrayList(RECORD_URI_ARG);
            }
            if (args.containsKey(FILE_URI_ARG)) {
                Parcelable[] filesParcel = args.getParcelableArray(FILE_URI_ARG);
                itemsFile = new ArrayList<>();

                for (Parcelable parcelable : filesParcel) {
                    File itemFile = new File(((Uri) parcelable).getPath());
                    itemsFile.add(itemFile);
                }
            }
        }
    }

    @Override
    protected void onNegativeClick() {
        ((DeleteDialogClickListener) mDialogListener).onDeleteCanceled();
    }

    @Override
    protected void onPositiveClick() {
        ((DeleteDialogClickListener) mDialogListener).onDeleteConfirmed(recordsUri, itemsFile);
    }

    public static DeleteDialogFragmentBuilder createBuilder(Context context, FragmentManager fragmentManager) {
        return new DeleteDialogFragmentBuilder(context, fragmentManager);
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class DeleteDialogFragmentBuilder extends SimpleDialogFragmentBuilder<DeleteDialogFragment> {

        public DeleteDialogFragmentBuilder(Context context, FragmentManager fragmentManager) {
            super(context, fragmentManager);
        }

        public DeleteDialogFragmentBuilder setRecordsUri(ArrayList<String> recordsUri) {
            args.putStringArrayList(RECORD_URI_ARG, recordsUri);
            return this;
        }

        public DeleteDialogFragmentBuilder setFiles(List<File> files) {
            List<Uri> filesUriList = new ArrayList<>();
            for (File file : files) {
                filesUriList.add(Uri.fromFile(file));
            }
            args.putParcelableArray(FILE_URI_ARG, filesUriList.toArray(new Uri[filesUriList.size()]));
            return this;
        }

        @Override
        public DeleteDialogFragment build() {
            return new DeleteDialogFragment();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface DeleteDialogClickListener extends DialogClickListener {
        void onDeleteConfirmed(List<String> itemToDelete, List<File> fileToDelete);

        void onDeleteCanceled();
    }

}
