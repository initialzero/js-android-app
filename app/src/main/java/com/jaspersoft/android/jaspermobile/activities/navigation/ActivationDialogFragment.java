package com.jaspersoft.android.jaspermobile.activities.navigation;

import android.accounts.Account;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.retrofit.sdk.account.AccountManagerUtil;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import fr.castorflex.android.circularprogressbar.CircularProgressDrawable;
import rx.Observable;
import rx.Subscription;
import rx.android.app.AppObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EFragment
class ActivationDialogFragment extends DialogFragment {

    @FragmentArg
    protected Account account;

    private Subscription activateSubscription;
    private OnActivationListener activationListener;

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (activateSubscription != null) {
            activateSubscription.unsubscribe();
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setMessage(getString(R.string.account_activating));
        progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                makeSubscription();
            }
        });
        progressDialog.setCanceledOnTouchOutside(false);

        CircularProgressDrawable circularProgressDrawable = new CircularProgressDrawable
                .Builder(getActivity())
                .colors(getResources().getIntArray(R.array.holo_colors))
                .sweepSpeed(1f)
                .strokeWidth(6)
                .style(CircularProgressDrawable.Style.ROUNDED).build();
        progressDialog.setIndeterminateDrawable(circularProgressDrawable);
        return progressDialog;
    }

    private void makeSubscription() {
        Observable<Account> activationTask = AccountManagerUtil.get(getActivity())
                .activateAccount(account)
                .subscribeOn(Schedulers.io())
                .cache();

        activateSubscription = AppObservable.bindFragment(this, activationTask)
                .flatMap(new Func1<Account, Observable<Fragment>>() {
                    @Override
                    public Observable<Fragment> call(Account account) {
                        Fragment currentFragment = getFragmentManager().findFragmentByTag(NavigationActivity.CURRENT_TAG);
                        String className = currentFragment.getClass().getName();
                        try {
                            Fragment fragment = (Fragment) Class.forName(className).newInstance();
                            return Observable.just(fragment);
                        } catch (Exception e) {
                            return Observable.error(e);
                        }
                    }
                }).subscribe(
                        new Action1<Fragment>() {
                            @Override
                            public void call(Fragment page) {
                                dismiss();
                                if (activationListener != null) {
                                    activationListener.onAccountActivation(page);
                                }
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                dismiss();
                            }
                        });
    }

    public void setActivationListener(OnActivationListener activationListener) {
        this.activationListener = activationListener;
    }

    public static interface OnActivationListener {
        void onAccountActivation(Fragment page);
    }
}
