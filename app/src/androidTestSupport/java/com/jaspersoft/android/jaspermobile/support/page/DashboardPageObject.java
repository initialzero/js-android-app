/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.support.page;

import android.graphics.Bitmap;
import android.support.test.espresso.web.webdriver.Locator;
import android.view.View;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.support.BitmapWrapper;

import org.hamcrest.Matcher;

import java.util.concurrent.TimeUnit;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.web.webdriver.DriverAtoms.findElement;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAction.getImage;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAction.waitForTextInDashboard;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAction.watch;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAction.zoomIn;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAction.zoomOut;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAssertion.hasView;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAssertion.isVisible;


/**
 * @author Andrew Tivodar
 * @since 2.5
 */
public class DashboardPageObject extends PageObject {

    public void dashboardMatches(Matcher<View> reportMatcher) {
        onView(withId(R.id.webView)).
                check(matches(reportMatcher));
    }

    public Bitmap getDashboardBitmap() {
        BitmapWrapper bitmapWrapper = new BitmapWrapper();
        onView(withId(R.id.webView))
                .perform(getImage(bitmapWrapper));
        return bitmapWrapper.getBitmap();
    }

    public void zoomInDashboard() {
        onView(withId(R.id.webView))
                .perform(zoomIn());
    }

    public void zoomOutDashboard() {
        onView(withId(R.id.webView))
                .perform(zoomOut());
    }

    public void awaitDashboard() {
        awaitDashboard("", "");
    }

    public void awaitFullDashboard() {
        awaitDashboard("//*[@data-componentid=\"13__Top_Fives_Report\"]", "Big Promo");
        awaitDashboard("//*[@data-componentid=\"Sales_Trend\"]", "Store Cost");
        awaitDashboard("//*[@data-componentid=\"Geo_Mix\"]", "Dairy");
        awaitDashboard("//*[@data-componentid=\"Store_Type_Metrics\"]", "Deluxe Supermarket");
        awaitDashboard("//*[@data-componentid=\"ASP_Performance\"]", "Food");
    }

    private void awaitDashboard(String xPath, String keyWord) {
        onView(isRoot()).
                perform(watch(hasView(withId(R.id.webView)), TimeUnit.SECONDS.toMillis(30)));
        onView(withId(R.id.webView)).
                perform(watch(isVisible(), TimeUnit.SECONDS.toMillis(30)));
        onView(withId(R.id.progressMessage)).
                check(doesNotExist());

        if (!xPath.isEmpty()) {
            waitForTextInDashboard(findElement(Locator.XPATH, xPath), keyWord, TimeUnit.SECONDS.toMillis(15));
        }
    }
}
