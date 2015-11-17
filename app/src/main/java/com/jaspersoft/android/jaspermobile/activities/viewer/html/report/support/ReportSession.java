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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support;

import org.androidannotations.annotations.EBean;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EBean(scope = EBean.Scope.Singleton)
public class ReportSession {
    private String requestId;
    private int totalPage;
    private final List<ExecutionObserver> observers = new CopyOnWriteArrayList<>();

    public void registerObserver(ExecutionObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(ExecutionObserver observer) {
        observers.remove(observer);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
        for (ExecutionObserver observer : observers) {
            observer.onRequestIdChanged(requestId);
        }
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
        for (ExecutionObserver observer : observers) {
            observer.onPagesLoaded(totalPage);
        }
    }

    public static class SimpleExecutionObserver implements ExecutionObserver {
        @Override
        public void onRequestIdChanged(String requestId) {
        }

        @Override
        public void onPagesLoaded(int totalPage) {
        }
    }

    public static interface ExecutionObserver {
        void onRequestIdChanged(String requestId);
        void onPagesLoaded(int totalPage);
    }
}
