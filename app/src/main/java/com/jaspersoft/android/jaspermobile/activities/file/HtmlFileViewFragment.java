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
