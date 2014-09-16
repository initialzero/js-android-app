package com.jaspersoft.android.jaspermobile.test.acceptance.library;

import com.google.android.apps.common.testing.testrunner.ActivityLifecycleMonitorRegistry;
import com.google.android.apps.common.testing.ui.espresso.Espresso;
import com.jaspersoft.android.jaspermobile.activities.repository.LibraryActivity_;
import com.jaspersoft.android.jaspermobile.test.utils.CommonTestModule;
import com.jaspersoft.android.jaspermobile.test.Failing;
import com.jaspersoft.android.jaspermobile.test.ProtoActivityInstrumentation;
import com.jaspersoft.android.jaspermobile.test.utils.TestResources;
import com.jaspersoft.android.jaspermobile.util.JsXmlSpiceServiceWrapper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.async.JsXmlSpiceService;
import com.jaspersoft.android.sdk.client.async.request.cacheable.GetResourceLookupsRequest;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookupsList;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.SpiceService;
import com.octo.android.robospice.request.SpiceRequest;
import com.octo.android.robospice.request.listener.RequestListener;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.hasTotalCount;
import static com.jaspersoft.android.jaspermobile.test.utils.espresso.JasperMatcher.swipeUp;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class LibraryPagePaginationTest extends ProtoActivityInstrumentation<LibraryActivity_> {
    private static final int LIMIT = 40;

    @Mock
    JsServerProfile mockServerProfile;
    @Mock
    JsRestClient mockRestClient;
    @Mock
    SpiceManager mockSpiceService;
    @Mock
    JsXmlSpiceServiceWrapper mockJsXmlSpiceServiceWrapper;

    final MockedSpiceManager mMockedSpiceManager = new MockedSpiceManager(JsXmlSpiceService.class);
    private ResourceLookupsList firstLookUp, secondLookUp;
    private ResourceFragmentInjector injector;

    public LibraryPagePaginationTest() {
        super(LibraryActivity_.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MockitoAnnotations.initMocks(this);

        firstLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_0_40");
        secondLookUp = TestResources.get().fromXML(ResourceLookupsList.class, "library_40_40");

        firstLookUp.setTotalCount(
                firstLookUp.getResourceLookups().size() + secondLookUp.getResourceLookups().size()
        );

        registerTestModule(new TestModule());
        when(mockRestClient.getServerProfile()).thenReturn(mockServerProfile);
        when(mockServerProfile.getUsernameWithOrgId()).thenReturn(USERNAME);
        when(mockServerProfile.getPassword()).thenReturn(PASSWORD);
        when(mockJsXmlSpiceServiceWrapper.getSpiceManager()).thenReturn(mMockedSpiceManager);

        ResourcesFragmentIdlingResource resourceFragmentInjector =
                new ResourcesFragmentIdlingResource();
        Espresso.registerIdlingResources(resourceFragmentInjector);
        injector = new ResourceFragmentInjector(resourceFragmentInjector);
        ActivityLifecycleMonitorRegistry.getInstance()
                .addLifecycleCallback(injector);
    }

    @Override
    protected void tearDown() throws Exception {
        ActivityLifecycleMonitorRegistry.getInstance()
                .removeLifecycleCallback(injector);
        super.tearDown();
    }

    @Failing
    public void ignoreScrollTo() throws InterruptedException {
        startActivityUnderTest();
        for (int i = 0; i < 3; i++) {
            onView(withId(android.R.id.list)).perform(swipeUp());
        }
        onView(withId(android.R.id.list)).check(hasTotalCount(firstLookUp.getTotalCount()));
    }

    public void testFirstItemClick() {

    }

    @Override
    public String getPageName() {
        return "library";
    }

    private class MockedSpiceManager extends SpiceManager {
        public MockedSpiceManager(Class<? extends SpiceService> spiceServiceClass) {
            super(spiceServiceClass);
        }

        public <T> void execute(final SpiceRequest<T> request, final Object requestCacheKey,
                                final long cacheExpiryDuration, final RequestListener<T> requestListener) {
            if (request instanceof GetResourceLookupsRequest) {
                int offset = ((GetResourceLookupsRequest) request).getSearchCriteria().getOffset();
                if (offset == LIMIT) {
                    requestListener.onRequestSuccess((T) secondLookUp);
                } else {
                    requestListener.onRequestSuccess((T) firstLookUp);
                }
            }
        }
    }

    private class TestModule extends CommonTestModule {
        @Override
        protected void semanticConfigure() {
            bind(JsRestClient.class).toInstance(mockRestClient);
            bind(JsXmlSpiceServiceWrapper.class).toInstance(mockJsXmlSpiceServiceWrapper);
        }
    }
}

