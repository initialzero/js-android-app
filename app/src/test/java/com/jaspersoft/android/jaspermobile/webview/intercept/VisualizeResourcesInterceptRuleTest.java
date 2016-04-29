package com.jaspersoft.android.jaspermobile.webview.intercept;

import android.os.Build;

import com.jaspersoft.android.jaspermobile.webview.WebRequest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.util.ReflectionHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.mockito.Mockito.when;

/**
 * @author Tom Koptel
 * @since 2.5
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class VisualizeResourcesInterceptRuleTest {
    private static final String[] WHITE_LIST = new String[]{
            "http://192.168.88.55:8088/jasperserver-pro-62/rest_v2/bundles?expanded=true",
            "http://192.168.88.55:8088/jasperserver-pro-62/rest_v2/settings/dateTimeSettings",
            "http://192.168.88.55:8088/jasperserver-pro-62/scripts/auth/loginSuccess.json"
    };

    @Mock
    WebRequest resourceRequest;
    private VisualizeResourcesInterceptRule ruleUnderTest;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        ruleUnderTest = VisualizeResourcesInterceptRule.getInstance();
    }

    @Test
    public void should_intercept_for_api_higher_than_lollipop() throws Exception {
        givenAndroidOfVersion(22);

        thenShouldInterceptRequests();
    }

    @Test
    public void should_intercept_for_api_equal_lollipop() throws Exception {
        givenAndroidOfVersion(21);

        thenShouldInterceptRequests();
    }

    @Test
    public void should_not_intercept_for_api_lower_than_lollipop() throws Exception {
        givenAndroidOfVersion(14);

        thenShouldNotInterceptRequests();
    }

    private void givenAndroidOfVersion(int version) {
        ReflectionHelpers.setStaticField(Build.VERSION.class, "SDK_INT", version);
    }

    private void thenShouldInterceptRequests() {
        interceptRequests(true);
    }

    private void thenShouldNotInterceptRequests() {
        interceptRequests(false);
    }

    private void interceptRequests(boolean flag) {
        for (String url : WHITE_LIST) {
            when(resourceRequest.getUrl()).thenReturn(url);
            assertThat("Should not intercept url: " + url, ruleUnderTest.shouldIntercept(resourceRequest), is(flag));
        }
    }
}