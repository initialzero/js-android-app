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

package com.jaspersoft.android.jaspermobile.activities.report.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.ui.view.activity.ToolbarActivity;
import com.jaspersoft.android.sdk.widget.report.renderer.Bookmark;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
public class BookmarksActivity extends ToolbarActivity implements BookmarksAdapter.BookmarkSelectListener {
    public static final String BOOKMARK_LIST_ARG = "bookmarkList";
    public static final String SELECTED_BOOKMARK_ARG = "selectedBookmark";

    @BindView(R.id.bookmarksToolbar)
    Toolbar bookmarksToolbar;
    @BindView(R.id.bookmarksList)
    ListView bookmarksList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bookmarks);
        ButterKnife.bind(this);
        initToolbar(getString(R.string.rv_ab_bookmarks));

        initBookmarkList();
    }

    @NotNull
    private List<Bookmark> fetchBookmarks() {
        Bundle extras = getIntent().getExtras();
        List<Bookmark> bookmarks = null;
        if (extras != null) {
            bookmarks = extras.getParcelableArrayList(BOOKMARK_LIST_ARG);
        }
        if (bookmarks == null) {
            throw new RuntimeException("Bookmarks should be provided");
        }
        return bookmarks;
    }

    private void initToolbar(String title) {
        bookmarksToolbar.setTitle(title);
        bookmarksToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initBookmarkList() {
        List<Bookmark> bookmarks = fetchBookmarks();
        BookmarksAdapter bookmarksAdapter = new BookmarksAdapter(this, this, bookmarks);
        bookmarksList.setAdapter(bookmarksAdapter);
    }

    @Override
    public void onBookmarkSelected(Bookmark bookmark) {
        Bundle bundle= new Bundle();
        bundle.putParcelable(SELECTED_BOOKMARK_ARG, bookmark);

        Intent returnIntent = new Intent();
        returnIntent.putExtras(bundle);

        setResult(RESULT_OK, returnIntent);
        finish();
    }
}
