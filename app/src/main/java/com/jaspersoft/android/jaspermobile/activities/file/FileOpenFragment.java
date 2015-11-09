package com.jaspersoft.android.jaspermobile.activities.file;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.resource.FileLookup;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;

import java.util.List;

import roboguice.fragment.RoboFragment;
import roboguice.inject.InjectView;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_file_open)
public class FileOpenFragment extends RoboFragment {

    @InjectView(R.id.btnOpenFile)
    protected Button openFile;
    @InjectView(android.R.id.message)
    protected TextView messageView;

    @InstanceState
    @FragmentArg
    protected FileLookup.FileType fileType;

    @InstanceState
    @FragmentArg
    protected String fileUri;

    private String mMimeType;

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        defineFileMimeType();
        tryToOpen();
    }

    private void defineFileMimeType(){
        switch (fileType) {
            case img:
                mMimeType = "image/*";
                break;
            case pdf:
            case xls:
            case xlsx:
            case docx:
            case ods:
            case odt:
            case pptx:
            case rtf:
                mMimeType = "application/" + fileType.name();
                break;
        }
    }

    private boolean canBeOpened() {
        if (mMimeType == null) return false;

        Intent openIntent = new Intent(Intent.ACTION_VIEW);
        openIntent.setDataAndType(null, mMimeType);
        PackageManager packageManager = getActivity().getPackageManager();
        List<ResolveInfo> suitableApps = packageManager.queryIntentActivities(openIntent, PackageManager.GET_INTENT_FILTERS);
        return suitableApps.size() > 0;
    }

    private void tryToOpen(){
        if (canBeOpened()) {
            showFileCanBeOpenedMessage();
        } else {
            showErrorMessage();
        }
    }

    private void showFileCanBeOpenedMessage(){
        openFile.setOnClickListener(new OpenFileClickListener());
        openFile.setText(R.string.fv_can_open_btn);
        messageView.setText(R.string.fv_can_open_message);
    }

    private void openFile(){

    }

    private void showErrorMessage(){
        openFile.setOnClickListener(new TryAgainClickListener());
        openFile.setText(R.string.fv_can_not_open_btn);
        messageView.setText(R.string.fv_can_not_open_message);
    }

    private class TryAgainClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            tryToOpen();
        }
    }

    private class OpenFileClickListener implements View.OnClickListener{
        @Override
        public void onClick(View v) {
            openFile();
        }
    }
}
