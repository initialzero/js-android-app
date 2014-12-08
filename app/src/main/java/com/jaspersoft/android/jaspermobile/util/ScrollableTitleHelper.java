package com.jaspersoft.android.jaspermobile.util;

import android.app.ActionBar;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EBean;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EBean(scope = EBean.Scope.Singleton)
public class ScrollableTitleHelper {
    public void injectTitle(Activity activity, CharSequence title) {
        ActionBar actionBar = activity.getActionBar();
        if (actionBar == null) return;

        actionBar.setTitle(title);
        int barTitleId = activity.getResources().getIdentifier("action_bar_title", "id", "android");

        TextView acionBarTitle = (TextView) activity.findViewById(barTitleId);
                LayoutInflater inflator = LayoutInflater.from(activity);
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
