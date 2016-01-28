package com.jaspersoft.android.jaspermobile.data.cache.report;

import com.jaspersoft.android.jaspermobile.domain.PageRequest;
import com.jaspersoft.android.jaspermobile.domain.ReportPage;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportPageCacheTest {
    private static final String REPORT_URI = "/my/uri";
    private static final String PAGE_POSITION = "100";
    private static final PageRequest PAGE_REQUEST = new PageRequest(REPORT_URI, PAGE_POSITION);
    private static final ReportPage ANY_PAGE = new ReportPage("page", true);

    private InMemoryReportPageCache mInMemoryReportPageCache;

    @Before
    public void setUp() throws Exception {
        mInMemoryReportPageCache = new InMemoryReportPageCache();
    }

    @Test
    public void should_effectively_persist() throws Exception {
        mInMemoryReportPageCache.put(PAGE_REQUEST, ANY_PAGE);
        ReportPage reportPage = mInMemoryReportPageCache.get(PAGE_REQUEST);
        assertThat(reportPage, is(ANY_PAGE));
        mInMemoryReportPageCache.evict(REPORT_URI);
        ReportPage reportPage1 = mInMemoryReportPageCache.get(PAGE_REQUEST);
        assertThat(reportPage1, is(nullValue()));
    }
}