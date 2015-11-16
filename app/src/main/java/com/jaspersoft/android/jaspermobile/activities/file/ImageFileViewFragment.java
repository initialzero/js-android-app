package com.jaspersoft.android.jaspermobile.activities.file;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

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

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), options);
        resourceImage.setImageBitmap(bitmap);
    }
}
