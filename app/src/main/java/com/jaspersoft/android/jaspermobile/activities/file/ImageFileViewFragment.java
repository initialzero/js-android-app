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

import roboguice.inject.InjectView;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_image_open)
public class ImageFileViewFragment extends FileLoadFragment {

    @InjectView(R.id.resourceImage)
    protected ImageView resourceImage;
    @InjectView(R.id.error_text)
    protected TextView errorText;

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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

        String decodedImgUri = Uri.fromFile(file).toString();
        ImageLoader.getInstance().displayImage(decodedImgUri, resourceImage);
    }
}
