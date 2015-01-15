/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.sdk;

import com.jaspersoft.android.jaspermobile.test.support.UnitTestSpecification;
import com.jaspersoft.android.retrofit.sdk.account.BasicAccountProvider;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class BasicAccountDataStorageTest extends UnitTestSpecification {

    private BasicAccountProvider storage;

    @Before
    public void setUp() {
        storage = BasicAccountProvider.get(getContext());
    }

    @Test
    public void testPutAccountNameMethod() {
        storage.putAccountName("cookie");
        assertThat(storage.getAccountName(), is("cookie"));
        assertThat(storage.getAccount(), notNullValue());
    }
}
