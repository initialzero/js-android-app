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
    private int totalPage;
    private final List<ExecutionObserver> observers = Lists.newArrayList();

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
