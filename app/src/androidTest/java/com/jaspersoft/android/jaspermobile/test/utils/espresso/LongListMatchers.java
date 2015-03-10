/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 *  http://community.jaspersoft.com/project/jaspermobile-android
 *
 *  Unless you have purchased a commercial license agreement from Jaspersoft,
 *  the following license terms apply:
 *
 *  This program is part of Jaspersoft Mobile for Android.
 *
 *  Jaspersoft Mobile is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Jaspersoft Mobile is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Jaspersoft Mobile for Android. If not, see
 *  <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.test.utils.espresso;

import android.support.test.espresso.matcher.BoundedMatcher;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;

/**
 * Static utility methods to create {@link org.hamcrest.Matcher} instances that can be applied to the data
 * objects created by.
 * <p>
 * These matchers are used by the
 * {@link android.support.test.espresso.Espresso#onData(org.hamcrest.Matcher)} API and are
 * applied against the data exposed by @{link android.widget.ListView#getAdapter()}.
 * </p>
 * <p>
 * In LongListActivity's case - each row is a Map containing 2 key value pairs. The key "STR" is
 * mapped to a String which will be rendered into a TextView with the R.id.item_content. The other
 * key "LEN" is an Integer which is the length of the string "STR" refers to. This length is
 * rendered into a TextView with the id R.id.item_size.
 * </p>
 */
public final class LongListMatchers {

  private LongListMatchers() { }


  /**
   * Creates a matcher against the text stored in R.id.item_content. This text is roughly
   * "item: $row_number".
   */
  public static Matcher<Object> withItemContent(String expectedText) {
    // use preconditions to fail fast when a test is creating an invalid matcher.
    checkNotNull(expectedText);
    return withItemContent(equalTo(expectedText));
  }

  /**
   * Creates a matcher against the text stored in R.id.item_content. This text is roughly
   * "item: $row_number".
   */
  @SuppressWarnings("rawtypes")
  public static Matcher<Object> withItemContent(final Matcher<String> itemTextMatcher) {
    // use preconditions to fail fast when a test is creating an invalid matcher.
    checkNotNull(itemTextMatcher);
    return new BoundedMatcher<Object, Map>(Map.class) {
      @Override
      public boolean matchesSafely(Map map) {
        return hasEntry(equalTo("STR"), itemTextMatcher).matches(map);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("with item content: ");
        itemTextMatcher.describeTo(description);
      }
    };
  }

  /**
   * Creates a matcher against the text stored in R.id.item_size. This text is the size of the text
   * printed in R.id.item_content.
   */
  public static Matcher<Object> withItemSize(int itemSize) {
    // use preconditions to fail fast when a test is creating an invalid matcher.
    checkArgument(itemSize > -1);
    return withItemSize(equalTo(itemSize));
  }

  /**
   * Creates a matcher against the text stored in R.id.item_size. This text is the size of the text
   * printed in R.id.item_content.
   */
  @SuppressWarnings("rawtypes")
  public static Matcher<Object> withItemSize(final Matcher<Integer> itemSizeMatcher) {
    // use preconditions to fail fast when a test is creating an invalid matcher.
    checkNotNull(itemSizeMatcher);
    return new BoundedMatcher<Object, Map>(Map.class) {
      @Override
      public boolean matchesSafely(Map map) {
        return hasEntry(equalTo("LEN"), itemSizeMatcher).matches(map);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("with item size: ");
        itemSizeMatcher.describeTo(description);
      }
    };
  }

    public static Matcher<View> withAdaptedData(final Matcher<Object> dataMatcher) {
        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("with class name: ");
                dataMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof AdapterView)) {
                    return false;
                }
                @SuppressWarnings("rawtypes")
                Adapter adapter = ((AdapterView) view).getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (dataMatcher.matches(adapter.getItem(i))) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

}
