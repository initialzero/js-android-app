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

package com.jaspersoft.android.jaspermobile.util.print;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.File;

import rx.Observable;
import rx.functions.Action1;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.1
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class AppPrinterTest {

    @Mock
    ResourceProvider resourceProvider;
    @Mock
    ResourcePrintJob resourcePrintJob;
    @Mock
    Action1<File> successCallback;
    @Mock
    Action1<Throwable> errorCallback;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        when(resourceProvider.provideResource()).thenReturn(Observable.<File>empty());
        when(resourcePrintJob.printResource()).thenReturn(successCallback);
        when(resourcePrintJob.reportError()).thenReturn(errorCallback);
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotCreatePrinterWithoutResourcePrintJob() {
        AppPrinter
                .builder(RuntimeEnvironment.application)
                .setResourcePrintJob(resourcePrintJob)
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void shouldNotCreatePrinterWithoutResourceProvider() {
        AppPrinter
                .builder(RuntimeEnvironment.application)
                .setResourceProvider(resourceProvider)
                .build();
    }

    @Test
    public void shouldMaintainCorrectCallOrder() {
        ResourcePrinter printer = AppPrinter
                .builder(RuntimeEnvironment.application)
                .setResourceProvider(resourceProvider)
                .setResourcePrintJob(resourcePrintJob)
                .build();

        printer.print();

        verify(resourceProvider).provideResource();
        verify(resourcePrintJob).printResource();
        verify(resourcePrintJob).reportError();
    }

    @Test
    public void shouldResumeTaskExecutionAfterPause() {
        ResourcePrinter printer = AppPrinter
                .builder(RuntimeEnvironment.application)
                .setResourceProvider(resourceProvider)
                .setResourcePrintJob(resourcePrintJob)
                .build();

        printer.print();
        printer.pause();
        printer.resume();

        verify(resourcePrintJob, times(2)).printResource();
        verify(resourcePrintJob, times(2)).reportError();
    }
}
