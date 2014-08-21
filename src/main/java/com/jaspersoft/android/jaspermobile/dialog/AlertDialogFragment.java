/*
* Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
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

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.view.View;

import eu.inmite.android.lib.dialogs.ISimpleDialogListener;
import eu.inmite.android.lib.dialogs.SimpleDialogFragment;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class AlertDialogFragment extends SimpleDialogFragment {
    protected final static String ARG_ICON = "icon";

    private View.OnClickListener mNeutralButtonListener;
    private View.OnClickListener mPositiveButtonListener;
    private View.OnClickListener mNegativeButtonListener;

    //---------------------------------------------------------------------
    // Static methods
    //---------------------------------------------------------------------

    public static AlertDialogBuilder createBuilder(Context context, FragmentManager fragmentManager) {
        return new AlertDialogBuilder(context, fragmentManager, AlertDialogFragment.class);
    }

    //---------------------------------------------------------------------
    // Protected methods
    //---------------------------------------------------------------------

    @Override
    protected Builder build(Builder builder) {
        super.build(builder);

        int icon = getIcon();
        if (icon != 0) {
            builder.setIcon(icon);
        }

        final String positiveButtonText = getPositiveButtonText();
        if (!TextUtils.isEmpty(positiveButtonText)) {
            if (mPositiveButtonListener == null) {
                builder.setPositiveButton(positiveButtonText, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ISimpleDialogListener listener = getDialogListener();
                        if (listener != null) {
                            listener.onPositiveButtonClicked(mRequestCode);
                        }
                        dismiss();
                    }
                });
            } else {
                builder.setPositiveButton(positiveButtonText, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mPositiveButtonListener.onClick(view);
                        dismiss();
                    }
                });
            }
        }

        final String negativeButtonText = getNegativeButtonText();
        if (!TextUtils.isEmpty(negativeButtonText)) {
            if (mNegativeButtonListener == null) {
                builder.setNegativeButton(negativeButtonText, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ISimpleDialogListener listener = getDialogListener();
                        if (listener != null) {
                            listener.onNegativeButtonClicked(mRequestCode);
                        }
                        dismiss();
                    }
                });
            } else {
                builder.setNegativeButton(negativeButtonText, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mNegativeButtonListener.onClick(view);
                        dismiss();
                    }
                });
            }
        }

        final String neutralButtonText = getNeutralButtonText();
        if (!TextUtils.isEmpty(neutralButtonText)) {
            if (mNeutralButtonListener == null) {
                builder.setNeutralButton(neutralButtonText, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ISimpleDialogListener listener = getDialogListener();
                        if (listener != null) {
                            listener.onNeutralButtonClicked(mRequestCode);
                        }
                        dismiss();
                    }
                });
            } else {
                builder.setNeutralButton(neutralButtonText, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mNeutralButtonListener.onClick(view);
                        dismiss();
                    }
                });
            }
        }

        return builder;
    }

    //---------------------------------------------------------------------
    // Helper methods
    //---------------------------------------------------------------------

    private int getIcon() {
        return getArguments().getInt(ARG_ICON);
    }

    //---------------------------------------------------------------------
    // Setters & Getters
    //---------------------------------------------------------------------

    public void setNeutralButtonListener(View.OnClickListener neutralButtonListener) {
        mNeutralButtonListener = neutralButtonListener;
    }

    public void setPositiveButtonListener(View.OnClickListener positiveButtonListener) {
        mPositiveButtonListener = positiveButtonListener;
    }

    public void setNegativeButtonListener(View.OnClickListener negativeButtonListener) {
        mNegativeButtonListener = negativeButtonListener;
    }

    //---------------------------------------------------------------------
    // Nested classes
    //---------------------------------------------------------------------

    public static class AlertDialogBuilder extends SimpleDialogBuilder {
        private int mIcon;
        private View.OnClickListener mNeutralButtonListener;
        private View.OnClickListener mPositiveButtonListener;
        private View.OnClickListener mNegativeButtonListener;

        protected AlertDialogBuilder(Context context, FragmentManager fragmentManager, Class<? extends AlertDialogFragment> clazz) {
            super(context, fragmentManager, clazz);
        }

        public AlertDialogBuilder setIcon(int iconResourceId) {
            mIcon = iconResourceId;
            return this;
        }

        public AlertDialogBuilder setNeutralButton(View.OnClickListener neutralButtonListener) {
            mNeutralButtonListener = neutralButtonListener;
            return this;
        }

        public AlertDialogBuilder setPositiveButton(View.OnClickListener positiveButtonListener) {
            mPositiveButtonListener = positiveButtonListener;
            return this;
        }

        public AlertDialogBuilder setNegativeButton(View.OnClickListener negativeButtonListener) {
            mNegativeButtonListener = negativeButtonListener;
            return this;
        }

        @Override
        protected Bundle prepareArguments() {
            Bundle args = super.prepareArguments();
            args.putInt(AlertDialogFragment.ARG_ICON, mIcon);
            return args;
        }

        @Override
        public DialogFragment show() {
            AlertDialogFragment fragment = (AlertDialogFragment) super.show();
            fragment.setPositiveButtonListener(mPositiveButtonListener);
            fragment.setNegativeButtonListener(mNegativeButtonListener);
            fragment.setNeutralButtonListener(mNeutralButtonListener);
            return fragment;
        }
    }

}
