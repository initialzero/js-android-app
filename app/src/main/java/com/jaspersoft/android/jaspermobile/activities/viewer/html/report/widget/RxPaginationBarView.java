package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.widget;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public final class RxPaginationBarView {
    private final AbstractPaginationView mPaginationBarView;

    RxPaginationBarView(AbstractPaginationView paginationBarView) {
        mPaginationBarView = paginationBarView;
    }

    public Observable<Integer> pagesChangeEvents() {
        return Observable.create(new PaginationBarViewPagesOnSubscribe(mPaginationBarView));
    }
}
