/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;

/**
 * @author Andrew Tivodar
 * @since 2.2
 */
@EFragment
public class TextInputControlDialogFragment extends BaseDialogFragment implements DialogInterface.OnShowListener{

    private final static String INPUT_CONTROL_ARG = "input_control";

    private AlertDialog icValueDialog;
    private EditText icValue;

    private InputControl mInputControl;

    @SystemService
    protected InputMethodManager inputMethodManager;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final View customLayout = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_ic_value, null);

        icValue = (EditText) customLayout.findViewById(R.id.icValue);

        // allow only numbers if data type is numeric
        if (mInputControl.getType() == InputControl.Type.singleValueNumber) {
            icValue.setInputType(InputType.TYPE_CLASS_NUMBER
                    | InputType.TYPE_NUMBER_FLAG_SIGNED | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        }

        String icName = mInputControl.getState().getValue();
        icValue.setText(icName);
        icValue.setSelection(icName.length());

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(customLayout);
        builder.setTitle(mInputControl.getLabel());
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newIcValue = TextInputControlDialogFragment.this.icValue.getText().toString();
                if (mDialogListener != null) {
                    ((InputControlValueDialogCallback) mDialogListener).onTextValueEntered(mInputControl, newIcValue);
                }
            }
        });
        builder.setNegativeButton(R.string.cancel, null);

        icValueDialog = builder.create();
        icValueDialog.setOnShowListener(this);
        return icValueDialog;
    }

    @Override
    public void onShow(DialogInterface dialog) {
        inputMethodManager.showSoftInput(icValue, 0);
    }

    @Override
    protected Class<InputControlValueDialogCallback> getDialogCallbackClass() {
        return InputControlValueDialogCallback.class;
    }

    @Override
    protected void initDialogParams() {
        super.initDialogParams();

        Bundle args = getArguments();
        if (args != null) {
            if (args.containsKey(INPUT_CONTROL_ARG)) {
                mInputControl = args.getParcelable(INPUT_CONTROL_ARG);
            }
        }
    }

    public static InputControlValueDialogFragmentBuilder createBuilder(FragmentManager fragmentManager) {
        return new InputControlValueDialogFragmentBuilder(fragmentManager);
    }

    //---------------------------------------------------------------------
    // Dialog Builder
    //---------------------------------------------------------------------

    public static class InputControlValueDialogFragmentBuilder extends BaseDialogFragmentBuilder<TextInputControlDialogFragment> {

        public InputControlValueDialogFragmentBuilder(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        public InputControlValueDialogFragmentBuilder setInputControl(InputControl inputControl) {
            args.putParcelable(INPUT_CONTROL_ARG, inputControl);
            return this;
        }

        @Override
        protected TextInputControlDialogFragment build() {
            return new TextInputControlDialogFragment_();
        }
    }

    //---------------------------------------------------------------------
    // Dialog Callback
    //---------------------------------------------------------------------

    public interface InputControlValueDialogCallback extends DialogClickListener {
        void onTextValueEntered(InputControl inputControl, String name);
    }
}
