/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.ui.dashboard;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.support.page.DashboardPageObject;
import com.jaspersoft.android.jaspermobile.support.page.LibraryPageObject;
import com.jaspersoft.android.jaspermobile.ui.view.activity.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.support.rule.AuthenticateProfileTestRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAssertion.exist;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAssertion.hasItems;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAssertion.isVisible;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAssertion.withIconResource;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DashboardTest {
    private LibraryPageObject libraryPageObject;
    private DashboardPageObject dashboardPageObject;

    @Rule
    public ActivityTestRule<NavigationActivity_> page = new ActivityTestRule<>(NavigationActivity_.class);

    @ClassRule
    public static TestRule authRule = AuthenticateProfileTestRule.create();

    @Before
    public void init() {
        dashboardPageObject = new DashboardPageObject();
        libraryPageObject = new LibraryPageObject();

        whenUserNavigatesToDashboard("1. Supermart Dashboard");
    }

    private void whenUserNavigatesToDashboard(String label) {
        libraryPageObject.awaitLibrary();
        libraryPageObject.clickOnItem(label);
    }

    @Test
    public void cancelRunDashboard() {
        dashboardPageObject.dashboardMatches(not(isVisible()));
        Espresso.pressBack();
        libraryPageObject.resourcesListMatches(hasItems());
    }

    @Test
    public void dashboardTitle() {
        dashboardPageObject.titleMatches(startsWith("1. Supermart"));
    }

    @Test
    public void favoriteDashboard() {
        dashboardPageObject.awaitDashboard();
        dashboardPageObject.clickMenuItem(anyOf(withText("Add to favorites"), withId(R.id.favoriteAction)));
        dashboardPageObject.menuItemMatches(withIconResource(R.drawable.ic_menu_star), anyOf(withText("Add to favorites"), withId(R.id.favoriteAction)));

        dashboardPageObject.clickMenuItem(anyOf(withText("Add to favorites"), withId(R.id.favoriteAction)));
        dashboardPageObject.menuItemMatches(withIconResource(R.drawable.ic_menu_star_outline), anyOf(withText("Add to favorites"), withId(R.id.favoriteAction)));
    }

    @Test
    public void favoriteItemHint() {
        dashboardPageObject.awaitDashboard();
        dashboardPageObject.longClickMenuItem(anyOf(withText("Add to favorites"), withId(R.id.favoriteAction)));
        dashboardPageObject.assertToastMessage("Add to favorites");
    }

    @Test
    public void aboutAction() {
        dashboardPageObject.awaitDashboard();
        dashboardPageObject.clickMenuItem(anyOf(withText("View Details"), withId(R.id.aboutAction)));
        dashboardPageObject.dialogTitleMatches("1. Supermart Dashboard");
    }

    @Test
    public void refreshDashboard() {
        dashboardPageObject.awaitFullDashboard();
        dashboardPageObject.clickMenuItem(anyOf(withText("Refresh"), withId(R.id.refreshAction)));
        dashboardPageObject.awaitFullDashboard();
    }

    @Test
    public void printAction() {
        dashboardPageObject.awaitDashboard();
        dashboardPageObject.menuItemAssertion(anyOf(withText("Print"), withId(R.id.printAction)), exist());
    }
}
