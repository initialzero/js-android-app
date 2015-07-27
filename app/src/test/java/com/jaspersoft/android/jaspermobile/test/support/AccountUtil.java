/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.support;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;

import com.jaspersoft.android.jaspermobile.util.account.JasperAccountManager;

import java.io.IOException;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public final class AccountUtil {

    private final Context mContext;

    private AccountUtil(Context context) {
        mContext = context;
    }

    public static AccountUtil get(Context context) {
        return new AccountUtil(context);
    }

    public AccountUtil removeAllAccounts() {
        JasperAccountManager managerUtil = JasperAccountManager.get(mContext);
        Account[] accounts = managerUtil.getAccounts();
        if (accounts.length > 0) {
            for (Account account : accounts) {
                AccountManagerFuture<Boolean> result = AccountManager.get(mContext).removeAccount(account, null, null);
                try {
                    result.getResult();
                } catch (OperationCanceledException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } catch (AuthenticatorException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        JasperAccountManager.get(mContext).deactivateAccount();
        return this;
    }

}
