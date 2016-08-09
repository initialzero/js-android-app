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

package com.jaspersoft.android.jaspermobile.ui.leftpanel;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.jaspersoft.android.jaspermobile.support.page.LeftPanelPageObject;
import com.jaspersoft.android.jaspermobile.support.page.LibraryPageObject;
import com.jaspersoft.android.jaspermobile.support.rule.AuthenticateProfileTestRule;
import com.jaspersoft.android.jaspermobile.ui.view.activity.NavigationActivity_;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class LeftPanelNavigationTest {

    private LeftPanelPageObject leftPanelPageObject = new LeftPanelPageObject();
    private LibraryPageObject libraryPageObject = new LibraryPageObject();

    @Rule
    public ActivityTestRule<NavigationActivity_> page = new ActivityTestRule<>(NavigationActivity_.class);

    @ClassRule
    public static TestRule authRule = AuthenticateProfileTestRule.create();

    @Before
    public void init() {
        libraryPageObject.awaitLibrary();
    }

    @Test
    public void leftPanelButtonClick() {
        leftPanelPageObject.clickBurgerButton();
        leftPanelPageObject.waitForLeftPanelMatches(isDisplayed());
        leftPanelPageObject.clickBackButton();
        leftPanelPageObject.waitForLeftPanelMatches(not(isDisplayed()));
    }

    @Test
    public void leftPanelSwipe() {
        leftPanelPageObject.swipeToOpenMenu();
        leftPanelPageObject.waitForLeftPanelMatches(isDisplayed());
        leftPanelPageObject.swipeToCloseMenu();
        leftPanelPageObject.waitForLeftPanelMatches(not(isDisplayed()));
    }

    @Test
    public void goToLibrary() {
        leftPanelPageObject.goToLibrary();
        leftPanelPageObject.waitForLeftPanelMatches(not(isDisplayed()));
        leftPanelPageObject.swipeToOpenMenu();
        leftPanelPageObject.libraryMatches(isSelected());
    }

    @Test
    public void goToRepository() {
        leftPanelPageObject.goToRepository();
        leftPanelPageObject.waitForLeftPanelMatches(not(isDisplayed()));
        leftPanelPageObject.swipeToOpenMenu();
        leftPanelPageObject.repositoryMatches(isSelected());
    }

    @Test
    public void goToRecentlyViewed() {
        leftPanelPageObject.goToRecentlyViewed();
        leftPanelPageObject.waitForLeftPanelMatches(not(isDisplayed()));
        leftPanelPageObject.swipeToOpenMenu();
        leftPanelPageObject.recentMatches(isSelected());
    }

    @Test
    public void goToFavorites() {
        leftPanelPageObject.goToFavorites();
        leftPanelPageObject.waitForLeftPanelMatches(not(isDisplayed()));
        leftPanelPageObject.swipeToOpenMenu();
        leftPanelPageObject.favoritesMatches(isSelected());
    }

    @Test
    public void goToSavedItems() {
        leftPanelPageObject.goToSavedItems();
        leftPanelPageObject.waitForLeftPanelMatches(not(isDisplayed()));
        leftPanelPageObject.swipeToOpenMenu();
        leftPanelPageObject.savedItemsMatches(isSelected());
    }

    @Test
    public void goToSettings() {
        leftPanelPageObject.goToSettings();
        leftPanelPageObject.titleMatches(is("Settings"));
    }

    @Test
    public void showAbout() {
        leftPanelPageObject.showAbout();
        leftPanelPageObject.dialogTitleMatches("About");
    }
}
