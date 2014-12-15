package com.jaspersoft.android.jaspermobile.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;

import org.androidannotations.annotations.EFragment;

import java.util.Date;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
@EFragment
public class RateAppDialog extends DialogFragment {

    private static final String TAG = RateAppDialog.class.getSimpleName();

    public static final String KEY_PREF_NEED_TO_RATE = "pref_need_to_rate";
    public static final String KEY_PREF_LAST_RATE_TIME = "pref_last_rate_time";
    public static final String KEY_PREF_APP_LAUNCH_COUNT_WITHOUT_RATE = "pref_app_launch_without_rate";

    public static final int PASSED_TIME_UNTIL_SHOW = 1729800000; // 2 days * 24 * 60 * 60 * 1000 (ms) ;
    public static final int LAUNCHES_UNTIL_SHOW = 2;

    public void show(Context context, FragmentManager fm) {
        DefaultPrefHelper prefHelper = DefaultPrefHelper_.getInstance_(context);
        if (!prefHelper.isRateDialogEnabled()) return;

        prefHelper.increaseNonRateLaunchCount();

        long lastAppLaunchTime = prefHelper.getLastRateTime();
        long currentTime = new Date().getTime();
        if (lastAppLaunchTime == 0) {
            prefHelper.setLastRateTime(currentTime);
            return;
        }

        long appLaunchCount = prefHelper.getNonRateLaunchCount();
        if(appLaunchCount != LAUNCHES_UNTIL_SHOW ||
                currentTime <= lastAppLaunchTime + PASSED_TIME_UNTIL_SHOW) return;

        prefHelper.setLastRateTime(currentTime);

        RateAppDialog dialogFragment =
                (RateAppDialog) fm.findFragmentByTag(TAG);
        if (dialogFragment == null) {
            dialogFragment = RateAppDialog_.builder().build();
            dialogFragment.show(fm ,TAG);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setMessage(R.string.h_rd_description)
                .setPositiveButton(R.string.h_rd_rate, rateClickListener)
                .setNegativeButton(R.string.h_rd_never, neverShowAgainClickListener)
                .setNeutralButton(R.string.h_rd_later, showLaterClickListener);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    private DialogInterface.OnClickListener rateClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            DefaultPrefHelper_.getInstance_(getActivity().getApplicationContext()).setRateDialogEnabled(false);
            Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
            Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
            try {
                startActivity(goToMarket);
            } catch (ActivityNotFoundException e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
            }
            dismiss();
        }
    };

    private DialogInterface.OnClickListener neverShowAgainClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            DefaultPrefHelper_.getInstance_(getActivity().getApplicationContext()).setRateDialogEnabled(false);
            dismiss();
        }
    };

    private DialogInterface.OnClickListener showLaterClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            DefaultPrefHelper_.getInstance_(getActivity().getApplicationContext()).resetNonRateLaunchCount();
            dismiss();
        }
    };
}
