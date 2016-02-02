package com.jaspersoft.android.jaspermobile.data.repository.report;

import com.jaspersoft.android.jaspermobile.data.JasperRestClient;
import com.jaspersoft.android.jaspermobile.data.repository.resource.InMemoryResourceRepository;
import com.jaspersoft.android.sdk.service.data.report.ReportResource;
import com.jaspersoft.android.sdk.service.rx.repository.RxRepositoryService;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import rx.Observable;
import rx.observers.TestSubscriber;

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
    ReportResource mReportResource;
    @Mock
    JasperRestClient mJasperRestClient;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        setupMocks();
        mInMemoryResourceRepository = new InMemoryResourceRepository(
                mJasperRestClient,
                criteriaMapper, resourceMapper);
    }

    @Test
    public void should_fetch_from_network() throws Exception {
        TestSubscriber<ReportResource> test = new TestSubscriber<>();
        mInMemoryResourceRepository.getReportResource(REPORT_URI).subscribe(test);
        test.assertNoErrors();

        verify(mRepositoryService).fetchReportDetails(REPORT_URI);
    }

    private void setupMocks() {
        when(mJasperRestClient.repositoryService()).thenReturn(Observable.just(mRepositoryService));
        when(mRepositoryService.fetchReportDetails(anyString()))
                .thenReturn(Observable.just(mReportResource));
    }
}