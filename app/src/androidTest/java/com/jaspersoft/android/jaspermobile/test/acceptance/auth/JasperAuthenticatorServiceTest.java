package com.jaspersoft.android.jaspermobile.test.acceptance.auth;

import android.content.Intent;
import android.os.IBinder;
import android.test.ServiceTestCase;
import android.test.suitebuilder.annotation.MediumTest;
import android.test.suitebuilder.annotation.SmallTest;

import com.jaspersoft.android.jaspermobile.auth.JasperAuthenticatorService;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class JasperAuthenticatorServiceTest extends ServiceTestCase<JasperAuthenticatorService> {

    public JasperAuthenticatorServiceTest() {
        super(JasperAuthenticatorService.class);
    }

    /**
     * Test basic startup/shutdown of Service
     */
    @SmallTest
    public void testStartable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), JasperAuthenticatorService.class);
        startService(startIntent);
    }

    /**
     * Test binding to service
     */
    @MediumTest
    public void testBindable() {
        Intent startIntent = new Intent();
        startIntent.setClass(getContext(), JasperAuthenticatorService.class);
        IBinder service = bindService(startIntent);
    }


}
