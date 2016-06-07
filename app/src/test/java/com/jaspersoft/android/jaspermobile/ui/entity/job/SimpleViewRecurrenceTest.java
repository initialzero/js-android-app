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

package com.jaspersoft.android.jaspermobile.ui.entity.job;

import android.os.Bundle;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class SimpleViewRecurrenceTest {
    @Test
    public void testSerializationIntoParcelabel() throws Exception {
        SimpleViewRecurrence.Unit unit = SimpleViewRecurrence.Unit.create("DAY", "dia");
        JobFormViewEntity.Recurrence recurrence = SimpleViewRecurrence.builder()
                .interval(100)
                .occurrence(200)
                .localizedLabel("Localized")
                .untilDate(new Date())
                .unit(unit)
                .build();

        JobFormViewEntity.OutputFormat format = JobFormViewEntity.OutputFormat.create("PDF", "PDF");
        JobFormViewEntity form = JobFormViewEntity.builder()
                .id(90)
                .version(0)
                .source("/report/uri")
                .jobName("Job name")
                .fileName("file name.txt")
                .folderUri("/folder/uri")
                .startDate(new Date())
                .outputFormats(Collections.singletonList(format))
                .recurrence(recurrence)
                .build();

        Bundle bundle = new Bundle();
        bundle.putParcelable("parcel", form);

        JobFormViewEntity deserialized = bundle.getParcelable("parcel");

        assertThat(deserialized.id(), is(form.id()));
        assertThat(deserialized.version(), is(form.version()));
        assertThat(deserialized.source(), is(form.source()));
        assertThat(deserialized.jobName(), is(form.jobName()));
        assertThat(deserialized.fileName(), is(form.fileName()));
        assertThat(deserialized.folderUri(), is(form.folderUri()));
        assertThat(deserialized.startDate(), is(form.startDate()));
        assertThat(deserialized.outputFormats(), hasItem(format));
        assertThat(deserialized.recurrence(), is(recurrence));
    }
}