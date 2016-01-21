package com.jaspersoft.android.jaspermobile.presentation.component;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface VisualizeView {
    Observable<Void> scriptLoadedEvent();

    Observable<Void> loadStartEvent();

    Observable<LoadCompleteEvent> loadCompleteEvent();

    Observable<ErrorEvent> loadErrorEvent();

    Observable<ReportCompleteEvent> reportCompleteEvent();

    Observable<PageLoadCompleteEvent> pageLoadCompleteEvent();

    Observable<PageLoadErrorEvent> pageLoadErrorEvent();

    Observable<MultiPageLoadEvent> multiPageLoadEvent();

    Observable<ExternalReferenceClickEvent> externalReferenceClickEvent();

    Observable<ExecutionReferenceClickEvent> executionReferenceClickEvent();

    Observable<ErrorEvent> windowErrorEvent();
}
