package com.jaspersoft.android.jaspermobile.activities.repository.fragment;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.jaspersoft.android.jaspermobile.R;

import static android.content.DialogInterface.OnCancelListener;
import static android.content.DialogInterface.OnShowListener;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ProgressDialogFragment extends DialogFragment {
    private static final String TAG = ProgressDialogFragment.class.getSimpleName();
    private DialogInterface.OnCancelListener onCancelListener;
    private DialogInterface.OnShowListener onShowListener;


    public void setOnCancelListener(OnCancelListener onCancelListener) {
        this.onCancelListener = onCancelListener;
    }

    public void setOnShowListener(OnShowListener onShowListener) {
        this.onShowListener = onShowListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.r_pd_running_report_msg));
        progressDialog.setOnCancelListener(onCancelListener);
        progressDialog.setOnShowListener(onShowListener);
        return progressDialog;
    }

    public static void show(FragmentManager fm,
                            OnCancelListener onCancelListener,
                            OnShowListener onShowListener) {
        ProgressDialogFragment dialogFragment = (ProgressDialogFragment)
                fm.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = new ProgressDialogFragment();
            dialogFragment.setOnCancelListener(onCancelListener);
            dialogFragment.setOnShowListener(onShowListener);
            dialogFragment.show(fm, TAG);
        }
    }

    public static void dismiss(FragmentManager fm) {
        ProgressDialogFragment dialogFragment = (ProgressDialogFragment)
                fm.findFragmentByTag(TAG);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
    }
}
