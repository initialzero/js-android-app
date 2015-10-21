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

package com.jaspersoft.android.jaspermobile.test.utils.espresso;

import android.app.Activity;
import android.content.res.Resources;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.Root;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewAssertion;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import javax.annotation.Nullable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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
            public void check(@Nullable View view, @Nullable NoMatchingViewException e) {
                @SuppressWarnings("rawtypes")
                AbsListView adapter = ((AbsListView) view);
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
        if (decorViewMatcher == null) {
            throw new IllegalArgumentException("Decor view null");
        }
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

    public static ViewAction setTime(final int hour, final int minute) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                TimePicker tp = (TimePicker) view;
                tp.setCurrentHour(hour);
                tp.setCurrentMinute(minute);
            }

            @Override
            public String getDescription() {
                return "Set the passed time into the TimePicker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(TimePicker.class);
            }
        };
    }

    public static ViewAction selectCurrentNumber(final int number) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker numberPicker = (NumberPicker) view;
                numberPicker.setValue(number);
            }

            @Override
            public String getDescription() {
                return "Set the passed value into the NumberPicker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }
        };
    }

    public static Matcher<? super View> hasMinValue(final int minValue) {
        return new TypeSafeMatcher<View>() {
            public int assertedMinValue = Integer.MIN_VALUE;

            @Override
            public boolean matchesSafely(View view) {
                NumberPicker numberPicker = (NumberPicker) view;
                assertedMinValue = numberPicker.getMinValue();
                return numberPicker.getMinValue() == minValue;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("expected minValue: "
                        + minValue + " but it was: " + assertedMinValue);
            }
        };
    }

    public static Matcher<? super View> hasErrorText(final String expectedError) {
        return new TypeSafeMatcher<View>() {
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
        };
    }

    public static Matcher<? super View> hasErrorText(final int resourceId) {
        return new BoundedMatcher<View, EditText>(EditText.class) {
            private String resourceName = null;
            private String expectedText = null;

            @Override
            public boolean matchesSafely(EditText editText) {
                if (null == expectedText) {
                    try {
                        expectedText = editText.getResources().getString(resourceId);
                        resourceName = editText.getResources().getResourceEntryName(resourceId);
                    } catch (Resources.NotFoundException ignored) {
            /* view could be from a context unaware of the resource id. */
                    }
                }
                if (null != expectedText) {
                    return expectedText.equals(editText.getError());
                } else {
                    return false;
                }
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with string from resource id: ");
                description.appendValue(resourceId);
                if (null != resourceName) {
                    description.appendText("[");
                    description.appendText(resourceName);
                    description.appendText("]");
                }
                if (null != expectedText) {
                    description.appendText(" value: ");
                    description.appendText(expectedText);
                }
            }
        };
    }

    public static Matcher<? super View> hasTextError(String expectedError) {
        return new HasTextMatcher(expectedError);
    }

    private static class HasTextMatcher extends TypeSafeMatcher<View> {
        private final String expectedText;

        private HasTextMatcher(String expectedError) {
            if (expectedError == null) {
                throw new IllegalArgumentException("Expected error should not be null");
            }
            this.expectedText = expectedError;
        }

        @Override
        public boolean matchesSafely(View view) {
            if (!(view instanceof EditText)) {
                return false;
            }
            EditText editText = (EditText) view;
            return expectedText.equals(editText.getText().toString());
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("with error: " + expectedText);
        }
    }
}
