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

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;

import com.jaspersoft.android.jaspermobile.visualize.ReportBookmark;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class BookmarksDialogFragment extends BaseDialogFragment implements DialogInterface.OnClickListener{

    private static final String BOOKMARKS_ARG = "bookmarks";

    private ArrayList<ReportBookmark> mReportBookmarks;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle("Bookmarks");

        CharSequence[] options = new CharSequence[mReportBookmarks.size()];
        for (int i = 0; i < mReportBookmarks.size(); i++) {
            options[i] = mReportBookmarks.get(i).getAnchor();
        }

        builder.setSingleChoiceItems(options, -1, this);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (mDialogListener != null) {
            ((BookmarksDialogClickListener) mDialogListener).onBookmarkSelected(mReportBookmarks.get(which));
        }
        dismiss();
    }

    @Override
    protected Class<BookmarksDialogClickListener> getDialogCallbackClass() {
        return BookmarksDialogClickListener.class;
    }

    protected void initDialogParams() {
        super.initDialogParams();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(BOOKMARKS_ARG)) {
                mReportBookmarks = (ArrayList<ReportBookmark>) args.getSerializable(BOOKMARKS_ARG);
            }
        }
    }

    public static BookmarksDialogFragmentBuilder createBuilder(FragmentManager fragmentManager) {
        return new BookmarksDialogFragmentBuilder(fragmentManager);
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class BookmarksDialogFragmentBuilder extends BaseDialogFragmentBuilder<BookmarksDialogFragment> {

        public BookmarksDialogFragmentBuilder(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public BookmarksDialogFragmentBuilder setBookmarksList(ArrayList<ReportBookmark> bookmarks) {
            args.putSerializable(BOOKMARKS_ARG, bookmarks);
            return this;
        }

        @Override
        protected BookmarksDialogFragment build() {
            return new BookmarksDialogFragment();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface BookmarksDialogClickListener extends DialogClickListener {
        void onBookmarkSelected(ReportBookmark reportBookmark);
    }
}
