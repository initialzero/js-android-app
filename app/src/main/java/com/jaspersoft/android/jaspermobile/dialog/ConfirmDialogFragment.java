/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import com.jaspersoft.android.jaspermobile.R;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EFragment
public class ConfirmDialogFragment extends DialogFragment {
    public static final String TAG = ConfirmDialogFragment.class.getSimpleName();

    @FragmentArg
    protected int titleId;
    @FragmentArg
    protected int messageId;

    private Observable<DialogInterface.OnClickListener> positiveClickListenerObservable;
    private final CompositeSubscription compositeSubscription = new CompositeSubscription();

    public static Builder builder(FragmentManager fragmentManager) {
        return new Builder(fragmentManager);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder
                .setTitle(titleId)
                .setMessage(messageId)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.spm_delete_btn,
                        new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog, final int which) {
                        Subscription subscription = positiveClickListenerObservable
                                .subscribe(new Action1<DialogInterface.OnClickListener>() {
                            @Override
                            public void call(DialogInterface.OnClickListener onClickListener) {
                                onClickListener.onClick(dialog, which);
                            }
                        });
                        compositeSubscription.add(subscription);
                    }
                });

        return builder.create();
    }

    public void setPositiveClickListener(DialogInterface.OnClickListener positiveClickListener) {
        this.positiveClickListenerObservable = Observable.just(positiveClickListener);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        compositeSubscription.unsubscribe();
    }

    public static class Builder {
        private int title;
        private int message;
        private DialogInterface.OnClickListener positiveClick;
        private final FragmentManager fragmentManager;

        public Builder(FragmentManager fragmentManager) {
            this.fragmentManager = fragmentManager;
        }

        public Builder title(int title) {
            this.title = title;
            return this;
        }

        public Builder message(int message) {
            this.message = message;
            return this;
        }

        public Builder positiveClick(DialogInterface.OnClickListener positiveClick) {
            this.positiveClick = positiveClick;
            return this;
        }

        public void show() {
            Fragment fragment = fragmentManager.findFragmentByTag(TAG);
            if (fragment == null) {
                ConfirmDialogFragment dialogFragment =
                        ConfirmDialogFragment_.builder()
                                .messageId(message)
                                .titleId(title)
                                .build();
                dialogFragment.setPositiveClickListener(positiveClick);
                dialogFragment.show(fragmentManager, TAG);
            }
        }
    }
}
