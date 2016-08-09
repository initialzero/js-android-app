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

package com.jaspersoft.android.jaspermobile.ui.reportview;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.jaspersoft.android.jaspermobile.support.page.LibraryPageObject;
import com.jaspersoft.android.jaspermobile.support.page.ReportPaginationPageObject;
import com.jaspersoft.android.jaspermobile.support.page.ReportViewPageObject;
import com.jaspersoft.android.jaspermobile.ui.view.activity.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.support.rule.AuthenticateProfileTestRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;

/**
 * @author Andrew Tivodar
 * @since 2.6
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ReportPaginationTest {

    private LibraryPageObject libraryPageObject;
    private ReportViewPageObject reportViewPageObject;
    private ReportPaginationPageObject reportPaginationPageObject;

    @Rule
    public ActivityTestRule<NavigationActivity_> page = new ActivityTestRule<>(NavigationActivity_.class);

    @ClassRule
    public static TestRule authRule = AuthenticateProfileTestRule.create();

    @Before
    public void init() {
        reportViewPageObject = new ReportViewPageObject();
        libraryPageObject = new LibraryPageObject();
        reportPaginationPageObject = new ReportPaginationPageObject();

        libraryPageObject.awaitLibrary();
        libraryPageObject.clickOnItem("05. Accounts Report");

        reportViewPageObject.waitForReportWithKeyWord("");
        reportPaginationPageObject.totalMatches(isDisplayed());
    }

    @Test
    public void paginationAppear() {
        reportViewPageObject.paginationMatches(isDisplayed());
    }

    @Test
    public void paginationTotalPageCountAppear() {
        reportPaginationPageObject.totalMatches(withText("of 47"));
    }

    @Test
    public void paginationNextPrevButton() {
        reportPaginationPageObject.clickNextPage();
        reportPaginationPageObject.currentMatches(withText("2"));

        reportPaginationPageObject.clickPrevPage();
        reportPaginationPageObject.currentMatches(withText("1"));
    }

    @Test
    public void paginationLastFirstButton() {
        reportPaginationPageObject.clickLastPage();
        reportPaginationPageObject.currentMatches(withText("47"));

        reportPaginationPageObject.clickFirstPage();
        reportPaginationPageObject.currentMatches(withText("1"));
    }

    @Test
    public void paginationDialogEnter() {
        reportPaginationPageObject.clickCurrentPage();
        reportPaginationPageObject.selectPage(4);
        reportPaginationPageObject.dialogPositiveButtonClick();

        reportPaginationPageObject.currentMatches(withText("4"));
    }

    @Test
    public void paginationDialogCancel() {
        reportPaginationPageObject.clickCurrentPage();
        reportPaginationPageObject.selectPage(3);
        reportPaginationPageObject.dialogNegativeButtonClick();

        reportPaginationPageObject.currentMatches(not(withText("3")));
    }
}
