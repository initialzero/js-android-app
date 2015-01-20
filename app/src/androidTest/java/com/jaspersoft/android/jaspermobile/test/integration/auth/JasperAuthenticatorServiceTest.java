/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.test.integration.auth;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.content.Intent;
import android.os.Bundle;
import android.test.ServiceTestCase;

import com.jaspersoft.android.jaspermobile.auth.JasperAuthenticator;
import com.jaspersoft.android.jaspermobile.auth.JasperAuthenticatorService;
import com.jaspersoft.android.retrofit.sdk.util.JasperSettings;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class JasperAuthenticatorServiceTest extends ServiceTestCase<JasperAuthenticatorService> {
    @Mock
    AccountAuthenticatorResponse response;

    public JasperAuthenticatorServiceTest() {
        super(JasperAuthenticatorService.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testAddAccount() throws NetworkErrorException {
        Intent  startIntent = new Intent();
        startIntent.setClass(getContext(), JasperAuthenticatorService.class);
        startService(startIntent);

        JasperAuthenticator authenticator = getService().getAuthenticator();
        Bundle bundle = authenticator.addAccount(response, JasperSettings.JASPER_ACCOUNT_TYPE, null, null, null);

        assertNotNull(bundle);
        assertTrue(bundle.containsKey(AccountManager.KEY_INTENT));

        Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
        assertThat(intent.getAction(), is(JasperSettings.ACTION_AUTHORIZE));
        assertTrue(intent.hasExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE));
        assertTrue(intent.getParcelableExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE) == response);
        getService().stopService(startIntent);
    }
}
