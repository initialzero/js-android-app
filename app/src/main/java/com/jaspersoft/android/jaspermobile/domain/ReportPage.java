/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.domain;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class ReportPage {
    public static final ReportPage EMPTY = new ReportPage(null, true);

    private final byte[] mContent;
    private final boolean mFinal;

    public ReportPage(byte[] content, boolean aFinal) {
        mContent = content;
        mFinal = aFinal;
    }

    public byte[] getContent() {
        return mContent;
    }

    public boolean isFinal() {
        return mFinal;
    }

    @Override
    public String toString() {
        return "ReportPage{" +
                "mContent='" + mContent + '\'' +
                ", mFinal=" + mFinal +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportPage that = (ReportPage) o;

        if (mFinal != that.mFinal) return false;
        return !(mContent != null ? !mContent.equals(that.mContent) : that.mContent != null);
    }

    @Override
    public int hashCode() {
        int result = mContent != null ? mContent.hashCode() : 0;
        result = 31 * result + (mFinal ? 1 : 0);
        return result;
    }

    public boolean isEmpty() {
        return mContent == null;
    }
}

