/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.sdk;

import com.jaspersoft.android.jaspermobile.test.support.UnitTestSpecification;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountDataStorage;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class BasicAccountDataStorageTest extends UnitTestSpecification {

    private BasicAccountDataStorage storage;

    @Before
    public void setUp() {
        storage = BasicAccountDataStorage.get(getContext());
    }

    @Test
    public void testCookiePropertyMethods() {
        storage.putCookie("cookie");
        assertThat(storage.getServerCookie(), is("cookie"));

        storage.setServerCookie("cookie1");
        assertThat(storage.getServerCookie(), is("cookie1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutCookieShouldNotAcceptNullValues() {
        storage.putCookie(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetCookieServerShouldNotAcceptNullValues() {
        storage.setServerCookie(null);
    }

    @Test
    public void testServerVersionPropertyMethods() {
        storage.putVersionName("5");
        assertThat(storage.getServerVersion(), is("5"));

        storage.setServerVersion("6");
        assertThat(storage.getServerVersion(), is("6"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutVersionNameShouldNotAcceptNullValues() {
        storage.putVersionName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetVersionNameShouldNotAcceptNullValues() {
        storage.setServerVersion(null);
    }

    @Test
    public void testServerEditionPropertyMethods() {
        storage.putEdition("PRO");
        assertThat(storage.getServerEdition(), is("PRO"));

        storage.setServerEdition("CE");
        assertThat(storage.getServerEdition(), is("CE"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutServerEditionNameShouldNotAcceptNullValues() {
        storage.putEdition(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetServerEditionNameShouldNotAcceptNullValues() {
        storage.setServerEdition(null);
    }

    @Test
    public void testServerAliasPropertyMethods() {
        storage.putServerAlias("ALIAS");
        assertThat(storage.getServerAlias(), is("ALIAS"));

        storage.setServerAlias("ALIAS1");
        assertThat(storage.getServerAlias(), is("ALIAS1"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPutServerAliasShouldNotAcceptNullValues() {
        storage.putServerAlias(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSetServerAliasShouldNotAcceptNullValues() {
        storage.setServerAlias(null);
    }

}
