package com.jaspersoft.android.jaspermobile.data.cache.report;

import com.jaspersoft.android.sdk.service.rx.report.RxReportExecution;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportCacheTest {

    private static final String REPORT_URI = "/my/uri";

    private InMemoryReportCache mInMemoryReportCache;

    @Mock
    RxReportExecution mReportExecution;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mInMemoryReportCache = new InMemoryReportCache();
    }

    @Test
    public void should_support_add_get_delete() throws Exception {
        mInMemoryReportCache.put(REPORT_URI, mReportExecution);
        RxReportExecution expected = mInMemoryReportCache.get(REPORT_URI);
        assertThat(expected, is(mReportExecution));

        mInMemoryReportCache.evict(REPORT_URI);
        expected = mInMemoryReportCache.get(REPORT_URI);
        assertThat(expected, is(nullValue()));
    }
}