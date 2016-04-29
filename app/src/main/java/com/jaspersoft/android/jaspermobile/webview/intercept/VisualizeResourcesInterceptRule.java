package com.jaspersoft.android.jaspermobile.webview.intercept;

import android.os.Build;

import com.jaspersoft.android.jaspermobile.webview.WebRequest;

/**
 * @author Tom Koptel
 * @since 2.5
 */
public class VisualizeResourcesInterceptRule implements WebResourceInterceptor.Rule {
    private static final String[] RESOURCES = new String[]{"bundles", "scripts", "settings"};

    private static class InstanceHolder {
        private static final VisualizeResourcesInterceptRule INSTANCE = new VisualizeResourcesInterceptRule();
    }

    private VisualizeResourcesInterceptRule() {}

    public static VisualizeResourcesInterceptRule getInstance() {
        return InstanceHolder.INSTANCE;
    }

    @Override
    public boolean shouldIntercept(WebRequest request) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean defaultVale = false;
            String url = request.getUrl().toLowerCase();
            for (String resource : RESOURCES) {
                defaultVale |= url.contains(resource);
            }
            return defaultVale;
        }
        return false;
    }
}
