package com.jaspersoft.android.jaspermobile.activities.file;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EFragment;

import java.io.File;
import java.util.List;

import roboguice.inject.InjectView;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_file_open)
public class ExternalOpenFragment extends FileLoadFragment {

    @InjectView(R.id.btnTryToOpen)
    protected Button tryToOpen;
    @InjectView(android.R.id.message)
    protected TextView messageView;

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tryToOpen.setOnClickListener(new TryAgainClickListener());

        tryToShowFile();
    }

    @Override
    protected void onFileReady(File file) {
        openFile(file);
    }

    @Override
    protected void showErrorMessage() {
        messageView.setText(R.string.fv_can_not_show_message);
    }

    private void showFileUnsupportedMessage() {
        messageView.setText(getString(R.string.sdr_t_no_app_available, fileType));
    }

    private void showFileOpeningMessage() {
        messageView.setText(R.string.fv_opening_message);
    }

    private void tryToShowFile() {
        if (canBeOpened()) {
            showFileOpeningMessage();
            tryToOpen.setVisibility(View.GONE);

            loadFile();
        } else {
            showFileUnsupportedMessage();
            tryToOpen.setVisibility(View.VISIBLE);
        }
    }

    private boolean canBeOpened() {
        String mimeType = "application/" + fileType.name();

        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(null, mimeType);
        PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> suitableApps = packageManager.queryIntentActivities(openIntent, PackageManager.GET_INTENT_FILTERS);
        return suitableApps.size() > 0;
    }

    private void openFile(File file) {
        String mimeType = "application/" + fileType.name();
        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(Uri.fromFile(file), mimeType);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            getActivity().startActivity(openIntent);
            getActivity().finish();
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getActivity(), getString(R.string.sdr_t_no_app_available, fileType), Toast.LENGTH_LONG).show();
        }
    }

    private class TryAgainClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            tryToShowFile();
        }
    }
}
