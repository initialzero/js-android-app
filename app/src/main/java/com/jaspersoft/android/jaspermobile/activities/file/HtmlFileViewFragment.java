package com.jaspersoft.android.jaspermobile.activities.file;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebView;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.cookie.CookieManagerFactory;

import org.androidannotations.annotations.EFragment;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import roboguice.inject.InjectView;
import rx.Subscription;
import rx.functions.Action1;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
@EFragment(R.layout.fragment_html_open)
public class HtmlFileViewFragment extends FileLoadFragment {

    @InjectView(R.id.resourceView)
    protected WebView resourceView;
    @InjectView(R.id.error_text)
    protected TextView errorText;

    private Subscription mCookieSubscription;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onViewCreated(View view, final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mCookieSubscription = CookieManagerFactory.syncCookies(getActivity()).subscribe(
                new Action1<Boolean>() {
                    @Override
                    public void call(Boolean aBoolean) {
                        loadFile();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        showErrorMessage();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mCookieSubscription.unsubscribe();
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

    private String getBaseUrl(){
        return jsRestClient.getServerProfile().getServerUrl() + "/fileview/fileview" + fileUri.subSequence(0, (fileUri.lastIndexOf('/') + 1));
    }
}
