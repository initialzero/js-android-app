package com.jaspersoft.android.jaspermobile.data.repository.report;

import com.jaspersoft.android.jaspermobile.data.entity.mapper.ResourceMapper;
import com.jaspersoft.android.jaspermobile.domain.AppResource;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;
import com.jaspersoft.android.sdk.service.rx.repository.RxRepositoryService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryResourceRepositoryTest {
    private static final String REPORT_URI = "/my/uri";
    private InMemoryResourceRepository mInMemoryResourceRepository;

    @Mock
    RxRepositoryService mRepositoryService;

    @Mock
    ResourceMapper mResourceMapper;
    @Mock
    AppResource mAppResource;
    @Mock
    ReportResource mReportResource;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mInMemoryResourceRepository = new InMemoryResourceRepository(
                mRepositoryService,
                mResourceMapper
        );
    }

    @Test
    public void should_fetch_from_network() throws Exception {
        TestSubscriber<AppResource> test = new TestSubscriber<>();
        mInMemoryResourceRepository.getReportResource(REPORT_URI).subscribe(test);
        test.assertNoErrors();

        verify(mResourceMapper).mapReportResource(mReportResource);
        verify(mRepositoryService).fetchReportDetails(REPORT_URI);
    }

    private void setupMocks() {
        when(mRepositoryService.fetchReportDetails(anyString()))
                .thenReturn(Observable.just(mReportResource));
        when(mResourceMapper.mapReportResource(any(ReportResource.class)))
                .thenReturn(mAppResource);
    }
}