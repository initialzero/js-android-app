package com.jaspersoft.android.jaspermobile.activities.navigation;

/**
 * @author Tom Koptel
 * @since 1.9
 */

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;

class FeedBackDialogFragment extends DialogFragment {
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.sa_show_feedback);
        builder.setMessage(R.string.sa_feedback_info);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("message/rfc822");
                        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"js.testdevice@gmail.com"});
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback");
                        try {
                            PackageInfo pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                            String versionName = pInfo.versionName;
                            String versionCode = String.valueOf(pInfo.versionCode);
                            intent.putExtra(Intent.EXTRA_TEXT, String.format("Version name: %s \nVersion code: %s", versionName, versionCode));
                        } catch (PackageManager.NameNotFoundException e) {
                        }
                        try {
                            getActivity().startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(getActivity(),
                                    getString(R.string.sdr_t_no_app_available, "email"),
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);

        return dialog;
    }
}