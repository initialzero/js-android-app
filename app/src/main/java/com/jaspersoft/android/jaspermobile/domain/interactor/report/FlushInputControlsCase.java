package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.domain.repository.report.ControlsRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class FlushInputControlsCase {
    private final ControlsRepository mControlsRepository;

    @Inject
    public FlushInputControlsCase(
            ControlsRepository controlsRepository
    ) {
        mControlsRepository = controlsRepository;
    }

    public void execute(String reportUri) {
        mControlsRepository.flushControls(reportUri);
    }
}
