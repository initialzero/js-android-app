package com.jaspersoft.android.jaspermobile.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class SimpleDialogFragment extends BaseDialogFragment {

    private final static String POSITIVE_BUTTON_TEXT_ARG = "positive_button_text";
    private final static String NEGATIVE_BUTTON_TEXT_ARG = "negative_button_text";
    private final static String MESSAGE_ARG = "message";
    private final static String TITLE_ARG = "title";
    private final static String ICON_RES_ARG = "icon_res";

    private String title;
    private String message;
    private String positiveButtonText;
    private String negativeButtonText;
    private int iconRes;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity())
                .setTitle(title)
                .setMessage(message);

        if (iconRes != 0) {
            dialogBuilder.setIcon(iconRes);
        }

        if (positiveButtonText != null) {
            dialogBuilder.setPositiveButton(positiveButtonText,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (mDialogListener != null) {
                                onPositiveClick();
                            }
                        }
                    });
        }
        if (negativeButtonText != null) {
            dialogBuilder.setNegativeButton(negativeButtonText,
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            if (mDialogListener != null) {
                                onNegativeClick();
                            }
                        }
                    }
            );
        }

        Dialog dialog = dialogBuilder.create();
        dialog.setCanceledOnTouchOutside(canceledOnTouchOutside);
        return dialog;
    }

    public static SimpleDialogFragmentBuilder createBuilder(Context context, FragmentManager fragmentManager) {
        return new SimpleDialogFragmentBuilder<SimpleDialogFragment>(context, fragmentManager);
    }

    protected void onPositiveClick() {
        ((SimpleDialogClickListener) mDialogListener).onPositiveClick(requestCode);
    }

    protected void onNegativeClick() {
        ((SimpleDialogClickListener) mDialogListener).onNegativeClick(requestCode);
    }

    protected void initDialogParams() {
        super.initDialogParams();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(POSITIVE_BUTTON_TEXT_ARG)) {
                positiveButtonText = args.getString(POSITIVE_BUTTON_TEXT_ARG);
            }
            if (args.containsKey(NEGATIVE_BUTTON_TEXT_ARG)) {
                negativeButtonText = args.getString(NEGATIVE_BUTTON_TEXT_ARG);
            }
            if (args.containsKey(MESSAGE_ARG)) {
                message = args.getString(MESSAGE_ARG);
            }
            if (args.containsKey(TITLE_ARG)) {
                title = args.getString(TITLE_ARG);
            }
            iconRes = args.getInt(ICON_RES_ARG, 0);
        }
    }

    @Override
    protected <T extends DialogClickListener> Class<T> getDialogCallbackClass() {
        return (Class<T>) SimpleDialogClickListener.class;
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class SimpleDialogFragmentBuilder<T extends SimpleDialogFragment> extends BaseDialogFragmentBuilder {

        private Context mContext;

        public SimpleDialogFragmentBuilder(Context context, FragmentManager fragmentManager) {
            super(fragmentManager);
            this.mContext = context;
        }

        public SimpleDialogFragmentBuilder<T> setTitle(String title) {
            args.putString(TITLE_ARG, title);
            return this;
        }

        public SimpleDialogFragmentBuilder<T> setTitle(int recourseId) {
            String title = mContext.getResources().getString(recourseId);
            args.putString(TITLE_ARG, title);
            return this;
        }

        public SimpleDialogFragmentBuilder<T> setMessage(String message) {
            args.putString(MESSAGE_ARG, message);
            return this;
        }

        public SimpleDialogFragmentBuilder<T> setMessage(int recourseId) {
            String message = mContext.getResources().getString(recourseId);
            args.putString(MESSAGE_ARG, message);
            return this;
        }

        public SimpleDialogFragmentBuilder<T> setPositiveButtonText(String positiveButtonText) {
            args.putString(POSITIVE_BUTTON_TEXT_ARG, positiveButtonText);
            return this;
        }

        public SimpleDialogFragmentBuilder<T> setPositiveButtonText(int recourseId) {
            String positiveButtonText = mContext.getResources().getString(recourseId);
            args.putString(POSITIVE_BUTTON_TEXT_ARG, positiveButtonText);
            return this;
        }

        public SimpleDialogFragmentBuilder<T> setNegativeButtonText(String negativeButtonText) {
            args.putString(NEGATIVE_BUTTON_TEXT_ARG, negativeButtonText);
            return this;
        }

        public SimpleDialogFragmentBuilder<T> setNegativeButtonText(int recourseId) {
            String negativeButtonText = mContext.getResources().getString(recourseId);
            args.putString(NEGATIVE_BUTTON_TEXT_ARG, negativeButtonText);
            return this;
        }

        public SimpleDialogFragmentBuilder<T> setIcon(int recourseId) {
            args.putInt(ICON_RES_ARG, recourseId);
            return this;
        }

        @Override
        protected BaseDialogFragment build() {
            return new SimpleDialogFragment();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface SimpleDialogClickListener extends DialogClickListener {
        public void onPositiveClick(int requestCode);

        public void onNegativeClick(int requestCode);
    }

}
