package com.jaspersoft.android.jaspermobile.data.cache.report;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class InMemoryVisualizeTemplateCacheTest {
    private InMemoryVisualizeTemplateCache inMemoryVisualizeTemplateCache;

    private final VisualizeTemplate fakeTemplate = new VisualizeTemplate("content", "http://server.url");
    private final Profile fakeProfile = Profile.create("any");

    @Before
    public void setUp() throws Exception {
        inMemoryVisualizeTemplateCache = new InMemoryVisualizeTemplateCache();
    }

    @Test
    public void testGet() throws Exception {
        inMemoryVisualizeTemplateCache.put(fakeProfile, fakeTemplate);
        VisualizeTemplate visualizeTemplate = inMemoryVisualizeTemplateCache.get(fakeProfile);
        assertThat(fakeTemplate, is(visualizeTemplate));
    }
}