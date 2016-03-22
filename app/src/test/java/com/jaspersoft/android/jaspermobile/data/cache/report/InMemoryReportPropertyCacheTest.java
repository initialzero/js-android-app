package com.jaspersoft.android.jaspermobile.data.cache.report;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryReportPropertyCacheTest {
    private static final String REPORT_URI = "/my/uri";
    private InMemoryReportPropertyCache mInMemoryReportPropertyCache;

    @Before
    public void setUp() throws Exception {
        mInMemoryReportPropertyCache = new InMemoryReportPropertyCache();
    }

    @Test
    public void should_support_add_get_delete() throws Exception {
        mInMemoryReportPropertyCache.putTotalPages(REPORT_URI, 100);
        Integer expected = mInMemoryReportPropertyCache.getTotalPages(REPORT_URI);
        assertThat(expected, is(100));

        mInMemoryReportPropertyCache.evict(REPORT_URI);
        expected = mInMemoryReportPropertyCache.getTotalPages(REPORT_URI);
        assertThat(expected, is(nullValue()));
    }
}
