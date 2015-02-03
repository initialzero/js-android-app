package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.support;

import com.google.common.collect.Lists;

import org.androidannotations.annotations.EBean;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.0
 */
@EBean(scope = EBean.Scope.Singleton)
public class ReportSession {
    private String requestId;
    private List<SessionObserver> observers = Lists.newArrayList();

    public void registerObserver(SessionObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(SessionObserver observer) {
        observers.remove(observer);
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
        for (SessionObserver observer : observers) {
            observer.onSessionChanged(requestId);
        }
    }

    public static interface SessionObserver {
        void onSessionChanged(String requestId);
    }
}
