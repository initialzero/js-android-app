package com.jaspersoft.android.jaspermobile.util;

import android.net.Uri;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.notNullValue;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class JsAssertions {

    private JsAssertions() {
        throw new AssertionError();
    }

    public static void assertNewUri(Uri uri) {
        assertThat(uri, notNullValue());
        assertThat(Long.valueOf(uri.getLastPathSegment()), greaterThan(0L));
    }


}
