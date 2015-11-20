package com.jaspersoft.android.jaspermobile.activities.file;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EFragment;

import java.io.File;

import roboguice.inject.InjectView;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_unsupported_file)
public class UnsupportedFilesFragment extends FileLoadFragment {

    @InjectView(android.R.id.message)
    protected TextView messageView;

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        showErrorMessage();
    }

    @Override
    protected void onFileReady(File file) {
    }

    @Override
    protected void showErrorMessage() {
        messageView.setText(R.string.fv_can_not_show_message);
    }
}
