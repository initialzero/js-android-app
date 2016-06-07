/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.activities.file;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.dialog.ProgressDialogFragment;

import org.androidannotations.annotations.EFragment;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_html_open)
public class HtmlFileViewFragment extends FileLoadFragment {

    protected WebView resourceView;
    protected TextView errorText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resourceView = (WebView) view.findViewById(R.id.resourceView);
        errorText = (TextView) view.findViewById(R.id.error_text);

        ProgressDialogFragment.builder(getFragmentManager())
                .setLoadingMessage(R.string.loading_msg)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        getActivity().finish();
                    }
                }).show();
        loadFile();
    }

    @Override
    protected void onFileReady(File file) {
        showHtml(file);
    }

    @Override
    protected void showErrorMessage() {
        errorText.setVisibility(View.VISIBLE);
    }

    private void showHtml(File file) {
        if (file == null) {
            showErrorMessage();
            return;
        }

        String resourceData;
        try {
            resourceData = FileUtils.readFileToString(file);
        } catch (IOException e) {
            showErrorMessage();
            return;
        }
        resourceView.getSettings().setUseWideViewPort(true);
        resourceView.getSettings().setLoadWithOverviewMode(true);

        resourceView.loadDataWithBaseURL(getBaseUrl(), resourceData, null, "UTF-8", null);
    }

    private String getBaseUrl() {
        return mServer.getBaseUrl() + "fileview/fileview" + fileUri.subSequence(0, (fileUri.lastIndexOf('/') + 1));
    }
}
