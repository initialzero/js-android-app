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

package com.jaspersoft.android.jaspermobile.ui.categories;

import android.content.Intent;
import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.support.page.RepositoryPageObject;
import com.jaspersoft.android.jaspermobile.support.page.LeftPanelPageObject;
import com.jaspersoft.android.jaspermobile.ui.view.activity.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.support.rule.AuthenticateProfileTestRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import static android.support.test.espresso.matcher.ViewMatchers.isSelected;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAssertion.hasImage;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAssertion.hasText;
import static com.jaspersoft.android.jaspermobile.support.matcher.AdditionalViewAssertion.withSearchViewHint;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RepositoryTest {

    private LeftPanelPageObject leftPanelPageObject;
    private RepositoryPageObject repositoryPageObject;

    @Rule
    public ActivityTestRule<NavigationActivity_> page = new ActivityTestRule<>(NavigationActivity_.class, false, false);

    @ClassRule
    public static TestRule authRule = AuthenticateProfileTestRule.create();

    @Before
    public void init() {
        repositoryPageObject = new RepositoryPageObject();
        leftPanelPageObject = new LeftPanelPageObject();

        Intent repoIntent = new Intent();
        repoIntent.putExtra("currentSelection", R.id.vg_repository);
        page.launchActivity(repoIntent);

        repositoryPageObject.awaitLibrary();
        givenPageWithDefaultViewType();
    }

    private void givenPageWithDefaultViewType() {
        repositoryPageObject.enforceViewType("List");
    }

    @Test
    public void repositoryAppear() {
        repositoryPageObject.resourceMatches(hasText("Organization"), 0);
    }

    @Test
    public void repositoryIsSelected() {
        leftPanelPageObject.clickBurgerButton();
        leftPanelPageObject.repositoryMatches(isSelected());
    }

    @Test
    public void repositoryTitle() {
        repositoryPageObject.titleMatches(is("Repository"));
    }

    @Test
    public void openFolder() {
        repositoryPageObject.clickOnItem("Public");
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.resourceMatches(hasText("Ad Hoc Components"), 0);
    }

    @Test
    public void folderTitle() {
        repositoryPageObject.clickOnItem("Public");
        repositoryPageObject.titleMatches(is("Public"));
    }

    @Test
    public void backFromFolder() {
        repositoryPageObject.clickOnItem("Public");
        Espresso.pressBack();
        repositoryPageObject.titleMatches(is("Repository"));
    }

    @Test
    public void repositorySearch() {
        repositoryPageObject.expandSearch();
        repositoryPageObject.searchFor("repo");
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.resourceMatches(hasText("Ad Hoc Reports"), 0);
    }

    @Test
    public void repositorySearchHint() {
        repositoryPageObject.expandSearch();
        repositoryPageObject.searchViewMatches(withSearchViewHint(containsString("Search resources")));
    }

    @Test
    public void repositoryIncorrectSearch() {
        repositoryPageObject.expandSearch();
        repositoryPageObject.searchFor("INCORRECT");
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.messageMatches(withText("No resources found"));
    }

    @Test
    public void viewTypeSwitch() {
        repositoryPageObject.changeViewType();
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.viewTypeMatches("Grid");

        repositoryPageObject.changeViewType();
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.viewTypeMatches("List");
    }

    @Test
    public void viewTypePersist() {
        repositoryPageObject.changeViewType();
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.viewTypeMatches("Grid");

        leftPanelPageObject.goToLibrary();
        leftPanelPageObject.goToRepository();
        repositoryPageObject.awaitLibrary();

        repositoryPageObject.viewTypeMatches("Grid");
    }

    @Test
    public void viewTypeSyncWithSearch() {
        repositoryPageObject.changeViewType();

        repositoryPageObject.expandSearch();
        repositoryPageObject.searchFor("repo");
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.viewTypeMatches("Grid");

        repositoryPageObject.changeViewType();
        Espresso.pressBack();
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.viewTypeMatches("List");
    }

    @Test
    public void emptyFolder() {
        repositoryPageObject.clickOnItem("Organization");
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.clickOnItem("Organizations");
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.messageMatches(withText("No resources found"));
    }

    @Test
    public void thumbnailAppear() {
        repositoryPageObject.clickOnItem("Public");
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.clickOnItem("Samples");
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.clickOnItem("Reports");
        repositoryPageObject.awaitLibrary();
        repositoryPageObject.resourceMatches(not(hasImage(R.drawable.ic_report)), 0);
    }
}
