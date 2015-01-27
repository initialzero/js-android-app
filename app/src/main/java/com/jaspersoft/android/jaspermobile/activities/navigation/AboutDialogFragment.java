package com.jaspersoft.android.jaspermobile.activities.navigation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.jaspersoft.android.jaspermobile.R;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class AboutDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sa_show_about);
        builder.setMessage(R.string.sa_about_info);
        builder.setCancelable(true);
        builder.setNeutralButton(android.R.string.ok, null);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                View decorView = getDialog().getWindow().getDecorView();
                if (decorView != null) {
                    TextView messageText = (TextView) decorView.findViewById(android.R.id.message);
                    if (messageText != null) {
                        messageText.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                }
            }
        });
        return dialog;
    }
}