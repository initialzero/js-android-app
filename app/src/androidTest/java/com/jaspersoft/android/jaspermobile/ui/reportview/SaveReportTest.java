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

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;

import com.jaspersoft.android.jaspermobile.R;
import com.jaspersoft.android.jaspermobile.support.page.LibraryPageObject;
import com.jaspersoft.android.jaspermobile.support.page.LeftPanelPageObject;
import com.jaspersoft.android.jaspermobile.support.page.ReportViewPageObject;
import com.jaspersoft.android.jaspermobile.support.page.SaveReportPageObject;
import com.jaspersoft.android.jaspermobile.ui.view.activity.NavigationActivity_;
import com.jaspersoft.android.jaspermobile.support.rule.AuthenticateProfileTestRule;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.startsWith;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SaveReportTest {

    private LeftPanelPageObject leftPanelPageObject;
    private LibraryPageObject libraryPageObject;
    private ReportViewPageObject reportViewPageObject;
    private SaveReportPageObject saveReportPageObject;

    @Rule
    public ActivityTestRule<NavigationActivity_> page = new ActivityTestRule<>(NavigationActivity_.class);
    @ClassRule
    public static TestRule authRule = AuthenticateProfileTestRule.create();

    @Before
    public void init() {
        reportViewPageObject = new ReportViewPageObject();
        libraryPageObject = new LibraryPageObject();
        saveReportPageObject = new SaveReportPageObject();
        leftPanelPageObject = new LeftPanelPageObject();

        libraryPageObject.awaitLibrary();
        libraryPageObject.clickOnItem("03. Store Segment");
        reportViewPageObject.awaitReport();
        saveReportPageObject.clickMenuItem(anyOf(withText("Save Report"), withId(R.id.saveReport)));
    }

    @Test
    public void savePageAppear() {
        reportViewPageObject.titleMatches(startsWith("Save Report"));
    }

    @Test
    public void navigationUp() {
        Espresso.pressBack();
        reportViewPageObject.awaitReport();
    }

    @Test
    public void saveWithEmptyName() {
        saveReportPageObject.typeFileName("");
        saveReportPageObject.clickSave();
        saveReportPageObject.fileNameErrorMatches("This field is required.");
    }

    @Test
    public void saveWithSpacedName() {
        saveReportPageObject.typeFileName("   ");
        saveReportPageObject.clickSave();
        saveReportPageObject.fileNameErrorMatches("This field is required.");
    }

    @Test
    public void saveInHtml() {
        saveReportPageObject.typeFileName("Test HTML saved item");
        saveReportPageObject.selectFormat("HTML");
        saveReportPageObject.clickSave();
        Espresso.pressBack();
        leftPanelPageObject.goToSavedItems();
        saveReportPageObject.savedItemMatches("Test HTML saved item",  R.drawable.ic_file_html);
    }

    @Test
    public void saveInPdf() {
        saveReportPageObject.typeFileName("Test PDF saved item");
        saveReportPageObject.selectFormat("PDF");
        saveReportPageObject.clickSave();
        Espresso.pressBack();
        leftPanelPageObject.goToSavedItems();
        saveReportPageObject.savedItemMatches("Test PDF saved item",  R.drawable.ic_file_pdf);
    }

    @Test
    public void saveInXls() {
        saveReportPageObject.typeFileName("Test XLS saved item");
        saveReportPageObject.selectFormat("XLS");
        saveReportPageObject.clickSave();
        Espresso.pressBack();
        leftPanelPageObject.goToSavedItems();
        saveReportPageObject.savedItemMatches("Test XLS saved item",  R.drawable.ic_file_xls);
    }

    @Test
    public void saveDuplication() {
        saveReportPageObject.typeFileName("Duplicate HTML saved item");
        saveReportPageObject.clickSave();

        reportViewPageObject.clickMenuItem(anyOf(withText("Save Report"), withId(R.id.saveReport)));

        saveReportPageObject.typeFileName("Duplicate HTML saved item");
        saveReportPageObject.clickSave();
        saveReportPageObject.fileNameErrorMatches("A file with this name already exists.");
    }

    @Test
    public void saveInDifferentFormat() {
        saveReportPageObject.typeFileName("Test saved report");
        saveReportPageObject.clickSave();

        reportViewPageObject.clickMenuItem(anyOf(withText("Save Report"), withId(R.id.saveReport)));
        saveReportPageObject.selectFormat("PDF");
        saveReportPageObject.typeFileName("Test saved report");
        saveReportPageObject.clickSave();

        reportViewPageObject.clickMenuItem(anyOf(withText("Save Report"), withId(R.id.saveReport)));
        saveReportPageObject.selectFormat("XLS");
        saveReportPageObject.typeFileName("Test saved report");
        saveReportPageObject.clickSave();

        Espresso.pressBack();
        leftPanelPageObject.goToSavedItems();
        saveReportPageObject.savedItemMatches("Test saved report", R.drawable.ic_file_html);
        saveReportPageObject.savedItemMatches("Test saved report", R.drawable.ic_file_pdf);
        saveReportPageObject.savedItemMatches("Test saved report", R.drawable.ic_file_html);
    }
}
