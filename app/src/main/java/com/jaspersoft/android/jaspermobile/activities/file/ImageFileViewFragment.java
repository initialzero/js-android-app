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

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.androidannotations.annotations.EFragment;

import java.io.File;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_image_open)
public class ImageFileViewFragment extends FileLoadFragment {

    protected ImageView resourceImage;
    protected TextView errorText;

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        resourceImage = (ImageView) view.findViewById(R.id.resourceImage);
        errorText = (TextView) view.findViewById(R.id.error_text);

        loadFile();
    }

    @Override
    protected void onFileReady(File file) {
        showImage(file);
    }

    @Override
    protected void showErrorMessage() {
        errorText.setVisibility(View.VISIBLE);
    }

    private void showImage(File file) {
        if (file == null) {
            showErrorMessage();
            return;
        }

        String imgUri = Uri.fromFile(file).toString();
        String decodedImgUri = Uri.decode(imgUri);
        ImageLoader.getInstance().displayImage(decodedImgUri, resourceImage);
    }
}
