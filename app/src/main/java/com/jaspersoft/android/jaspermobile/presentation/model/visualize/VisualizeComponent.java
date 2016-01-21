package com.jaspersoft.android.jaspermobile.presentation.model.visualize;


import com.jaspersoft.android.sdk.network.entity.report.ReportParameter;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface VisualizeComponent {
    VisualizeEvents visualizeEvents();

    VisualizeComponent run();

    VisualizeComponent loadPage(int page);

    VisualizeComponent update(List<ReportParameter> parameters);

    VisualizeComponent refresh();
}
