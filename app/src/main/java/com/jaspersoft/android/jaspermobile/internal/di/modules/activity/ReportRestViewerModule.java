package com.jaspersoft.android.jaspermobile.internal.di.modules.activity;

import com.jaspersoft.android.jaspermobile.internal.di.PerActivity;
import com.jaspersoft.android.jaspermobile.presentation.action.ReportActionListener;
import com.jaspersoft.android.jaspermobile.presentation.presenter.ReportViewPresenter;

import dagger.Module;
import dagger.Provides;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@Module
public final class ReportRestViewerModule extends ReportModule {
    public ReportRestViewerModule(String uri) {
        super(uri);
    }

    @Provides
    @PerActivity
    ReportActionListener provideReportActionListener(ReportViewPresenter presenter) {
        return presenter;
    }
}
