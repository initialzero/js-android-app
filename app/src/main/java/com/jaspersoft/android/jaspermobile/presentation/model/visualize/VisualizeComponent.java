package com.jaspersoft.android.jaspermobile.presentation.model.visualize;

import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface VisualizeComponent {
    VisualizeEvents visualizeEvents();

    void loadPage(int page);

    void update(List<ReportParameter> parameters);

    void refresh();
}
