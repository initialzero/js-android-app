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

package com.jaspersoft.android.jaspermobile.widget;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

/**
 * @author Tom Koptel
 * @since 1.9
 */
class AutoLayerDrawable extends LayerDrawable {

    // The color filter to apply when the button is pressed
    protected ColorFilter _pressedFilter = new LightingColorFilter(Color.LTGRAY, 1);
    // Alpha value when the button is disabled
    protected int _disabledAlpha = 100;
    // Alpha value when the button is enabled
    protected int _fullAlpha = 255;

    public AutoLayerDrawable(Drawable d) {
        super(new Drawable[]{d});
    }

    @Override
    protected boolean onStateChange(int[] states) {
        boolean enabled = false;
        boolean pressed = false;

        for (int state : states) {
            if (state == android.R.attr.state_enabled)
                enabled = true;
            else if (state == android.R.attr.state_pressed)
                pressed = true;
        }

//        mutate();
        if (enabled && pressed) {
            setColorFilter(_pressedFilter);
        } else if (!enabled) {
            setColorFilter(null);
            setAlpha(_disabledAlpha);
        } else {
            setColorFilter(null);
            setAlpha(_fullAlpha);
        }

        invalidateSelf();

        return super.onStateChange(states);
    }

    @Override
    public boolean isStateful() {
        return true;
    }
}
