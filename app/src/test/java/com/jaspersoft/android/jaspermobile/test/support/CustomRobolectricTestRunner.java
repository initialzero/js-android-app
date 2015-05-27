/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.test.support;

import android.os.Build;

import com.jaspersoft.android.jaspermobile.db.JSDatabaseHelper;

import org.junit.runners.model.InitializationError;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.SdkConfig;
import org.robolectric.bytecode.ClassInfo;
import org.robolectric.bytecode.Setup;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public class CustomRobolectricTestRunner extends RobolectricTestRunner {
    public CustomRobolectricTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
        // Pragma keys supported only on FROYO and higher
        Robolectric.Reflection.setFinalStaticField(Build.VERSION.class, "SDK_INT", 8);
    }

    @Override
    protected ClassLoader createRobolectricClassLoader(Setup setup, SdkConfig sdkConfig) {
        return super.createRobolectricClassLoader(new ExtraShadows(setup), sdkConfig);
    }

    class ExtraShadows extends Setup {
        private Setup setup;

        public ExtraShadows(Setup setup) {
            this.setup = setup;
        }

        public boolean shouldInstrument(ClassInfo classInfo) {
            boolean shoudInstrument = setup.shouldInstrument(classInfo);
            return shoudInstrument
                    || classInfo.getName().equals(JSDatabaseHelper.class.getName());
        }
    }
}
