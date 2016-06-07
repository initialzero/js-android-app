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

package com.jaspersoft.android.jaspermobile.domain.entity.job;

import com.jaspersoft.android.jaspermobile.domain.entity.Resource;

import java.util.Date;

/**
 * @author Andrew Tivodar
 * @since 2.5
 */
public class JobResource extends Resource {
    public final static int NORMAL = 1;
    public final static int EXECUTING = 2;
    public final static int COMPLETE = 3;
    public final static int PAUSED = 4;
    public final static int ERROR = 5;
    public final static int UNKNOWN = 6;

    private final int mId;
    private final Date mPreviousFireDate;
    private final Date mFireDate;
    private final int mState;
    private final String mDescription;
    private final JobTarget mJobTarget;
    private final String mOwner;

    public JobResource(String label, int id, Date previousFireDate, Date fireDate, int state, String description, JobTarget jobTarget, String owner) {
        super(label);

        this.mId = id;
        mPreviousFireDate = previousFireDate;
        this.mFireDate = fireDate;
        this.mState = state;
        mDescription = description;
        mJobTarget = jobTarget;
        mOwner = owner;
    }

    @Override
    public int getId() {
        return mId;
    }

    public Date getPreviousFireDate() {
        return mPreviousFireDate;
    }

    public Date getFireDate() {
        return mFireDate;
    }

    public int getState() {
        return mState;
    }

    public String getDescription() {
        return mDescription;
    }

    public JobTarget getJobTarget() {
        return mJobTarget;
    }

    public String getOwner() {
        return mOwner;
    }
}
