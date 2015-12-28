/*
 * Copyright � 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.util.cast;

import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.cast.CastPresentation;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class ReportPresentationService extends CastRemoteDisplayLocalService {

    private ReportPresentation mPresentation;
    private PresentationShowListener mPresentationShowListener;

    @Override
    public void onCreatePresentation(Display display) {
        onDismissPresentation();
        mPresentation = new ReportPresentation(this, display);

        try {
            mPresentation.show();
            if (mPresentationShowListener != null) {
                mPresentationShowListener.onShow();
            }
        } catch (WindowManager.InvalidDisplayException ex) {
            onDismissPresentation();
        }
    }

    @Override
    public void onDismissPresentation() {
        if (mPresentation != null) {
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    public void setPresentationShowListener(PresentationShowListener presentationShowListener) {
        mPresentationShowListener = presentationShowListener;
    }

    public ReportPresentation getReportPresentation() {
        return mPresentation;
    }

    public interface PresentationShowListener {
        void onShow();
    }
}
