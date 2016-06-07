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

package com.jaspersoft.android.jaspermobile.data.entity.mapper;

import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupSearchCriteria;
import com.jaspersoft.android.sdk.service.repository.RepositorySearchCriteria;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class CriteriaMapperTest {
    @Test
    public void should_map_legacy_criteria_to_retrofitted() throws Exception {
        ResourceLookupSearchCriteria criteria = new ResourceLookupSearchCriteria();
        criteria.setFolderUri("/");
        criteria.setTypes(Arrays.asList("reportUnit", "dashboard"));
        criteria.setQuery("all accounts");
        criteria.setRecursive(true);
        criteria.setOffset(0);
        criteria.setLimit(1);

        CriteriaMapper criteriaMapper = new CriteriaMapper();

        RepositorySearchCriteria searchCriteria = criteriaMapper.toRetrofittedCriteria(criteria);
        assertThat(searchCriteria.getFolderUri(), is("/"));
        assertThat(searchCriteria.getQuery(), is("all accounts"));
        assertThat(searchCriteria.getRecursive(), is(true));
        assertThat(searchCriteria.getLimit(), is(1));
        assertThat(searchCriteria.getOffset(), is(0));
        assertThat(searchCriteria.getResourceMask(), is(RepositorySearchCriteria.REPORT | RepositorySearchCriteria.DASHBOARD));
    }
}