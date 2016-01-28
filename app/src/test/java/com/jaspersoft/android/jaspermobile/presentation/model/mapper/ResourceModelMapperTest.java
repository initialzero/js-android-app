package com.jaspersoft.android.jaspermobile.presentation.model.mapper;

import com.jaspersoft.android.jaspermobile.presentation.model.ReportResourceModel;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ResourceModelMapperTest {

    private static final Date CREATION_DATE = new Date();

    @Mock
    ReportResource mReportResource;

    private ResourceModelMapper mResourceModelMapper;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mResourceModelMapper = new ResourceModelMapper();

        when(mReportResource.getLabel()).thenReturn("label");
        when(mReportResource.getDescription()).thenReturn("description");
        when(mReportResource.getUri()).thenReturn("/my/uri");
        when(mReportResource.getCreationDate()).thenReturn(CREATION_DATE);
    }

    @Test
    public void should_map_api_resource_to_view_model() throws Exception {
        ReportResourceModel expected = mResourceModelMapper.mapReportModel(mReportResource);
        assertThat(expected.getLabel(), is("label"));
        assertThat(expected.getDescription(), is("description"));
        assertThat(expected.getUri(), is("/my/uri"));
        assertThat(expected.getCreationDate().getTime(), is(CREATION_DATE.getTime()));
    }
}