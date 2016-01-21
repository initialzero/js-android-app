package com.jaspersoft.android.jaspermobile.domain.repository.report;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface VisualizeTemplateRepository {
    Observable<VisualizeTemplate> get(Profile profile, double diagonal);
}
