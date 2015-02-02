package com.jaspersoft.android.jaspermobile.util;

import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
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

        int barTitleId = activity.getResources().getIdentifier("action_bar_title", "id", "android");

        TextView acionBarTitle = (TextView) activity.findViewById(barTitleId);
                LayoutInflater inflator = LayoutInflater.from(activity);
        if (acionBarTitle == null) {
            return;
        }
        ViewGroup actionBarTitleParent = ((ViewGroup)acionBarTitle.getParent());
        actionBarTitleParent.removeView(acionBarTitle);

        View scrollContainer = inflator.inflate(R.layout.scrollable_title_container,
                actionBarTitleParent, false);
        ViewGroup container = (ViewGroup) scrollContainer.findViewById(R.id.container);
        container.addView(acionBarTitle);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) acionBarTitle.getLayoutParams();
        params.addRule(RelativeLayout.CENTER_IN_PARENT);
        acionBarTitle.setLayoutParams(params);

        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setCustomView(scrollContainer);
    }

}
