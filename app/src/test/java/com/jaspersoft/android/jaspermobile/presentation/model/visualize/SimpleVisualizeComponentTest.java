package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import android.webkit.WebView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.spy;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, sdk = 21)
public class SimpleVisualizeComponentTest {

    @Mock
    VisualizeEvents mVisualizeEvents;
    private SimpleVisualizeComponent mSimpleVisualizeComponent;
    private WebView webView;

    @Before
    public void setUp() throws Exception {
        webView = spy(new WebView(RuntimeEnvironment.application));
        mSimpleVisualizeComponent = new SimpleVisualizeComponent(webView, mVisualizeEvents);
    }

    @Test
    public void testRun() throws Exception {
        VisualizeExecOptions options = new VisualizeExecOptions("/my/uri", "{}");
        mSimpleVisualizeComponent.run(options);
        verify(webView).loadUrl("javascript:MobileReport.configure({}).run({\"uri\": \"/my/uri\",\"params\": {}})");
    }

    @Test
    public void testLoadPage() throws Exception {
        mSimpleVisualizeComponent.loadPage("1");
        verify(webView).loadUrl("javascript:MobileReport.selectPage(1)");
    }

    @Test
    public void testUpdate() throws Exception {
        mSimpleVisualizeComponent.update("{}");
        verify(webView).loadUrl("javascript:MobileReport.applyReportParams({})");
    }

    @Test
    public void testRefresh() throws Exception {
        mSimpleVisualizeComponent.refresh();
        verify(webView).loadUrl("javascript:MobileReport.refresh()");
    }
}