/*
 * Copyright (c) 2015. Lorem ipsum dolor sit amet, consectetur adipiscing elit.
 * Morbi non lorem porttitor neque feugiat blandit. Ut vitae ipsum eget quam lacinia accumsan.
 * Etiam sed turpis ac ipsum condimentum fringilla. Maecenas magna.
 * Proin dapibus sapien vel ante. Aliquam erat volutpat. Pellentesque sagittis ligula eget metus.
 * Vestibulum commodo. Ut rhoncus gravida arcu.
 */

package com.jaspersoft.android.jaspermobile.sdk;

import android.util.Base64;

import com.jaspersoft.android.jaspermobile.test.support.UnitTestSpecification;
import com.jaspersoft.android.retrofit.sdk.token.BasicAccessTokenEncoder;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class BasicAccessTokenEncoderTest extends UnitTestSpecification {

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderShouldNotAcceptNullUsername() {
        BasicAccessTokenEncoder.builder()
                .setUsername(null)
                .setPassword("my_password")
                .setOrganization(null)
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderShouldNotAcceptNullPassword() {
        BasicAccessTokenEncoder.builder()
                .setUsername("username")
                .setPassword(null)
                .setOrganization(null)
                .build();
    }

    @Test
    public void testInstanceShouldReturnEncodedValue() {
        BasicAccessTokenEncoder encoder = BasicAccessTokenEncoder.builder()
                .setUsername("username")
                .setPassword("1234")
                .setOrganization(null)
                .build();
        String token = encoder.encodeToken();

        assertThat(token, notNullValue());
    }

    @Test
    public void testBasicImplementationConsumesBase64() {
        BasicAccessTokenEncoder encoder = BasicAccessTokenEncoder.builder()
                .setUsername("username")
                .setPassword("1234")
                .setOrganization(null)
                .build();
        String token = encoder.encodeToken();

        assertThat(token, containsString("Basic "));

        String hash = token.split(" ")[1];
        String rawString = new String(Base64.decode(hash, Base64.NO_WRAP));
        assertThat(rawString, is("username:1234"));
    }

    @Test
    public void testEncodesOrganizationValueAsWell() {
        BasicAccessTokenEncoder encoder = BasicAccessTokenEncoder.builder()
                .setUsername("username")
                .setPassword("1234")
                .setOrganization("organization")
                .build();

        String token = encoder.encodeToken();

        assertThat(token, containsString("Basic "));

        String hash = token.split(" ")[1];
        String rawString = new String(Base64.decode(hash, Base64.NO_WRAP));
        assertThat(rawString, is("username|organization:1234"));
    }

}
