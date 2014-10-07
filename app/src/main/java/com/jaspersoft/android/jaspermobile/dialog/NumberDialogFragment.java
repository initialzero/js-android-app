package com.jaspersoft.android.jaspermobile.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.NumberPicker;

import com.jaspersoft.android.jaspermobile.R;
import com.negusoft.holoaccent.dialog.AccentAlertDialog;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

/**
 * @author Tom Koptel
 * @since 1.9
 */
@EFragment
public class NumberDialogFragment extends DialogFragment {

    private static final String TAG = NumberDialogFragment.class.getSimpleName();

    @FragmentArg
    int totalPages;
    @FragmentArg
    int currentPage;

    private OnPageSelectedListener onPageSelectedListener;

    public static void show(FragmentManager fm, int currentPage, int totalPages,
                            OnPageSelectedListener onPageSelectedListener) {
        NumberDialogFragment dialogFragment = (NumberDialogFragment)
                fm.findFragmentByTag(TAG);

        if (dialogFragment == null) {
            dialogFragment = NumberDialogFragment_.builder()
                    .totalPages(totalPages).currentPage(currentPage).build();
            dialogFragment.setPageSelectedListener(onPageSelectedListener);
            dialogFragment.show(fm, TAG);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AccentAlertDialog.Builder builder = new AccentAlertDialog.Builder(getActivity());

        ViewGroup customView = (ViewGroup) LayoutInflater.from(getActivity())
                .inflate(R.layout.number_dialog_layout, null);
        final NumberPicker numberPicker = (NumberPicker)
                customView.findViewById(R.id.numberPicker);
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(totalPages);

        builder.setTitle(R.string.dialog_current_page);
        builder.setView(customView);
        builder.setCancelable(true);
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (onPageSelectedListener != null) {
                    onPageSelectedListener.onPageSelected(numberPicker.getValue());
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                numberPicker.setValue(currentPage);
            }
        });

        return dialog;
    }

    public void setPageSelectedListener(OnPageSelectedListener onPageSelectedListener) {
        this.onPageSelectedListener = onPageSelectedListener;
    }

    public static interface OnPageSelectedListener {
        void onPageSelected(int page);
    }

}
