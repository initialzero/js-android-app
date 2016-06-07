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

package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.FakePostExecutionThread;
import com.jaspersoft.android.jaspermobile.FakePreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.repository.report.VisualizeTemplateRepository;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Map;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class GetVisualizeTemplateCaseTest {

    @Mock
    VisualizeTemplateRepository mVisualizeTemplateRepository;

    private final Profile fakeProfile = Profile.create("fake");
    private GetVisualizeTemplateCase mGetVisualizeTemplateCase;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mGetVisualizeTemplateCase = new GetVisualizeTemplateCase(
                FakePreExecutionThread.create(),
                FakePostExecutionThread.create(),
                mVisualizeTemplateRepository,
                fakeProfile);
    }

    @Test
    public void testBuildUseCaseObservable() throws Exception {
        when(mVisualizeTemplateRepository.get(any(Profile.class), anyMap()))
                .thenReturn(Observable.just(new VisualizeTemplate("content", "url")));

        TestSubscriber<VisualizeTemplate> test = new TestSubscriber<>();
        Map<String, Object> params = Collections.emptyMap();
        mGetVisualizeTemplateCase.execute(params, test);

        verify(mVisualizeTemplateRepository).get(fakeProfile, params);
}
}