package com.jaspersoft.android.jaspermobile.presentation.navigation;

import android.content.Context;
import android.content.Intent;

import com.jaspersoft.android.jaspermobile.presentation.view.activity.AuthenticatorActivity;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class SignUpPage extends Page {
    public SignUpPage(Context context) {
        super(context);
    }

    @Override
    Intent getIntent() {
        return new Intent(getContext(), AuthenticatorActivity.class);
    }
}
