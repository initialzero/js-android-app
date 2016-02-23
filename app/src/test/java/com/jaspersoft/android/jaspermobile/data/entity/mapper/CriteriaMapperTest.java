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