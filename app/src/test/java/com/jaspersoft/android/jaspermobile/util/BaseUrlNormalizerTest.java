package com.jaspersoft.android.jaspermobile.util;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class BaseUrlNormalizerTest {
    @Test
    public void normalize_should_return_same_value_if_null_passed() throws Exception {
        assertThat(BaseUrlNormalizer.normalize(null), is(nullValue()));
    }

    @Test
    public void normalize_should_return_same_value_if_empty_string_passed() throws Exception {
        assertThat(BaseUrlNormalizer.normalize(""), is(""));
    }

    @Test
    public void normalize_should_append_path_if_one_missing() throws Exception {
        assertThat(BaseUrlNormalizer.normalize("http://localhost"), is("http://localhost/"));
    }

    @Test
    public void normalize_should_not_append_path_if_one_exists() throws Exception {
        assertThat(BaseUrlNormalizer.normalize("http://localhost/"), is("http://localhost/"));
    }

    @Test
    public void denormalize_should_remove_path_if_one_exists() throws Exception {
        assertThat(BaseUrlNormalizer.denormalize("http://localhost/"), is("http://localhost"));
    }
}