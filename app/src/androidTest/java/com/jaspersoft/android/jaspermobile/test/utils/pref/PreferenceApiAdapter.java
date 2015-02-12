package com.jaspersoft.android.jaspermobile.test.utils.pref;

import android.content.Context;

import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper_;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class PreferenceApiAdapter implements PreferenceApi {

    public static PreferenceApi init(Context context) {
        return new PreferenceApiAdapter(context);
    }

    private final DefaultPrefHelper_ helper;

    private PreferenceApiAdapter(Context context) {
        this.helper = DefaultPrefHelper_.getInstance_(context);
    }

    @Override
    public PreferenceApi setIntroEnabled(boolean value) {
        helper.needToShowIntro(value);
        return this;
    }

    @Override
    public PreferenceApi setInAppAnimationEnabled(boolean value) {
        helper.setAnimationEnabled(value);
        return this;
    }

    @Override
    public PreferenceApi setCacheEnabled(boolean value) {
        helper.setRepoCacheEnabled(value);
        return this;
    }

}
