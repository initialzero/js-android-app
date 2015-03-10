package com.jaspersoft.android.jaspermobile.util;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean
public class ScrollableTitleHelper {

    @RootContext
    protected ActionBarActivity activity;

    public void injectTitle(CharSequence title) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar == null) return;
        actionBar.setTitle(title);

        TextView toolBarTitle = null;
        ViewGroup toolBar = (ViewGroup) activity.findViewById(R.id.tb_navigation);
        if (toolBar == null) return;

        int toolbarChildCount = toolBar.getChildCount();
        for (int i = 0; i < toolbarChildCount; i++) {
            View view = toolBar.getChildAt(i);
            if (view instanceof TextView) {
                toolBarTitle = (TextView) view;
                break;
            }
        }

        if (toolBarTitle == null) return;
        toolBar.removeView(toolBarTitle);

        LayoutInflater layoutInflater = LayoutInflater.from(activity);
        View scrollContainer = layoutInflater.inflate(R.layout.scrollable_title_container,
                null, false);
        ViewGroup container = (ViewGroup) scrollContainer.findViewById(R.id.container);
        container.addView(toolBarTitle);

        toolBar.addView(scrollContainer);
    }

}
