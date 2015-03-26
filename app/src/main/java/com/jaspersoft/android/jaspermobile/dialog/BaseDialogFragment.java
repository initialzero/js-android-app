package com.jaspersoft.android.jaspermobile.dialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public abstract class BaseDialogFragment extends DialogFragment {

    private final static String CANCELED_ON_TOUCH_OUTSIDE_ARG = "canceled_on_touch_outside";

    protected boolean canceledOnTouchOutside;
    protected DialogClickListener mDialogListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        initDialogParams();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        attachListener(activity, getDialogCallbackClass());
    }

    protected void initDialogParams() {
        Bundle args = getArguments();
        if (args != null) {
            canceledOnTouchOutside = args.getBoolean(CANCELED_ON_TOUCH_OUTSIDE_ARG, true);
        }
    }

    protected abstract <T extends DialogClickListener> Class<T> getDialogCallbackClass();

    private <T extends DialogClickListener> void attachListener(Activity activity, Class<T> callbackClass) {
        Fragment targetFragment = getTargetFragment();
        if (targetFragment == null) {
            try {
                mDialogListener = callbackClass.cast(activity);
            } catch (ClassCastException e) {
                mDialogListener = null;
            }
        } else {
            try {
                mDialogListener = callbackClass.cast(targetFragment);
            } catch (ClassCastException e) {
                mDialogListener = null;
            }
        }
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static abstract class BaseDialogFragmentBuilder<T extends BaseDialogFragment> {

        protected final Bundle args;
        protected final FragmentManager mFragmentManager;
        protected Fragment mTargetFragment;

        public BaseDialogFragmentBuilder(FragmentManager fragmentManager) {
            this.args = new Bundle();
            this.mFragmentManager = fragmentManager;
        }

        public BaseDialogFragmentBuilder<T> setTargetFragment(Fragment targetFragment) {
            mTargetFragment = targetFragment;
            return this;
        }

        public BaseDialogFragmentBuilder<T> setCancelableOnTouchOutside(boolean canceledOnTouchOutside) {
            args.putBoolean(CANCELED_ON_TOUCH_OUTSIDE_ARG, canceledOnTouchOutside);
            return this;
        }

        protected abstract T build();

        public void show() {
            T baseDialogFragment = build();
            if (mTargetFragment != null) {
                baseDialogFragment.setTargetFragment(mTargetFragment, 0);
            }

            baseDialogFragment.setArguments(args);
            baseDialogFragment.show(mFragmentManager, baseDialogFragment.getClass().getSimpleName());
        }

    }


    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    protected interface DialogClickListener {
    }
}
