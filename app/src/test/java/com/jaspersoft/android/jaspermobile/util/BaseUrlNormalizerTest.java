/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

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