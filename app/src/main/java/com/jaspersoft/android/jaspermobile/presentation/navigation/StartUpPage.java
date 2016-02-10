package com.jaspersoft.android.jaspermobile.presentation.navigation;

import android.content.Context;
import android.content.Intent;

import com.jaspersoft.android.jaspermobile.presentation.view.activity.StartupActivity;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class StartUpPage extends Page {
    public StartUpPage(Context context) {
        super(context);
    }

    @Override
    Intent getIntent() {
        Intent intent = new Intent(getContext(), StartupActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return intent;
    }
}
