/*
* Copyright (C) 2012 Jaspersoft Corporation. All rights reserved.
* http://community.jaspersoft.com/project/jaspermobile-android
*
* Unless you have purchased a commercial license agreement from Jaspersoft,
* the following license terms apply:
*
* This program is part of Jaspersoft Mobile for Android.
*
* Jaspersoft Mobile is free software: you can redistribute it and/or modify
* it under the terms of the GNU Lesser General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* Jaspersoft Mobile is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU Lesser General Public License for more details.
*
* You should have received a copy of the GNU Lesser General Public License
* along with Jaspersoft Mobile for Android. If not, see
* <http://www.gnu.org/licenses/lgpl>.
*/

package com.jaspersoft.android.jaspermobile.test.utils.espresso;

import android.app.Activity;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;

import com.google.android.apps.common.testing.ui.espresso.NoMatchingViewException;
import com.google.android.apps.common.testing.ui.espresso.Root;
import com.google.android.apps.common.testing.ui.espresso.ViewAction;
import com.google.android.apps.common.testing.ui.espresso.ViewAssertion;
import com.google.android.apps.common.testing.ui.espresso.ViewInteraction;
import com.google.android.apps.common.testing.ui.espresso.action.GeneralLocation;
import com.google.android.apps.common.testing.ui.espresso.action.GeneralSwipeAction;
import com.google.android.apps.common.testing.ui.espresso.action.Press;
import com.google.android.apps.common.testing.ui.espresso.action.Swipe;
import com.google.common.base.Optional;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static com.google.android.apps.common.testing.ui.espresso.Espresso.onView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.RootMatchers.withDecorView;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withId;
import static com.google.android.apps.common.testing.ui.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public final class JasperMatcher {
    private JasperMatcher() {
        throw new AssertionError();
    }

    public static Matcher<View> firstChildOf(final Matcher<View> parentMatcher) {
        return childForPositionOf(0, parentMatcher);
    }

    public static Matcher<View> childForPositionOf(final int position, final Matcher<View> parentMatcher) {
        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("with child on position " + position + " of parent " + parentMatcher.toString());
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view.getParent() instanceof ViewGroup)) {
                    return parentMatcher.matches(view.getParent());
                }
                ViewGroup group = (ViewGroup) view.getParent();
                if (parentMatcher.matches(view.getParent())) {
                    int childCount = group.getChildCount();
                    if (position >= childCount) {
                        throw new RuntimeException("Position '" + position + "' should be lower than child count '" + childCount + "'");
                    }
                    return group.getChildAt(position).equals(view);
                } else {
                    return false;
                }
            }
        };
    }

    public static ViewAction scrollToListItem() {
        return new ScrollToItemAction();
    }

    public static ViewAction swipeDown() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.TOP_CENTER,
                GeneralLocation.BOTTOM_CENTER, Press.FINGER);
    }

    public static ViewAction swipeUp() {
        return new GeneralSwipeAction(Swipe.FAST, GeneralLocation.BOTTOM_CENTER,
                GeneralLocation.TOP_CENTER, Press.FINGER);
    }

    public static ViewAssertion hasTotalCount(final int totalCount) {
        return new ViewAssertion() {
            @Override
            public void check(Optional<View> view, Optional<NoMatchingViewException> noView) {
                @SuppressWarnings("rawtypes")
                AbsListView adapter = ((AbsListView) view.get());
                assertThat(adapter.getAdapter().getCount(), is(totalCount));
            }
        };
    }

    public static ViewInteraction onViewDialogId(Activity activity, int id) {
        return onView(withId(id))
                .inRoot(withDecorView(
                        is(not(activity.getWindow().getDecorView()))
                ));
    }

    public static ViewInteraction onViewDialogText(Activity activity, int resId) {
        return onView(withText(resId))
                .inRoot(withDecorView(
                        is(not(activity.getWindow().getDecorView()))
                ));
    }

    public static ViewInteraction onOverflowView(Activity activity, final Matcher<View> viewMatcher) {
        return onView(viewMatcher).inRoot(withDecorView(
                is(not(activity.getWindow().getDecorView()))
        ));
    }

    public static Matcher<Root> withNotDecorView(final Matcher<View> decorViewMatcher) {
        checkNotNull(decorViewMatcher);
        return new TypeSafeMatcher<Root>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("with decor view ");
                decorViewMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(Root root) {
                return decorViewMatcher.matches(root.getDecorView());
            }
        };
    }

    public static Matcher<View> refreshing() {
        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("with decor view ");
            }

            @Override
            public boolean matchesSafely(View view) {
                if ((view instanceof SwipeRefreshLayout)) return false;
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) view;
                return swipeRefreshLayout.isRefreshing();
            }
        };
    }

    public static Matcher<? super View> hasErrorText(String expectedError) {
        return new ErrorTextMatcher(expectedError);
    }

    private static class ErrorTextMatcher extends TypeSafeMatcher<View> {
        private final String expectedError;

        private ErrorTextMatcher(String expectedError) {
            this.expectedError = checkNotNull(expectedError);
        }

        @Override
        public boolean matchesSafely(View view) {
            if (!(view instanceof EditText)) {
                return false;
            }
            EditText editText = (EditText) view;
            return expectedError.equals(editText.getError());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("with error: " + expectedError);
        }
    }
}
