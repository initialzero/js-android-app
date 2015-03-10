package com.jaspersoft.android.jaspermobile.activities.viewer.html.report;

import android.support.v4.app.Fragment;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public interface FragmentCreator<FRAGMENT extends Fragment, DATA> {
    FRAGMENT createFragment(DATA data);
}
