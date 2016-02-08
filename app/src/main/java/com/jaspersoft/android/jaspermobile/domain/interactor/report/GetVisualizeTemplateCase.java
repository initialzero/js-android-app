package com.jaspersoft.android.jaspermobile.domain.interactor.report;

import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.VisualizeTemplate;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.report.VisualizeTemplateRepository;
import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;

import java.util.Map;

import javax.inject.Inject;

import rx.Observable;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public class GetVisualizeTemplateCase extends AbstractUseCase<VisualizeTemplate, Map<String, ?>> {

    private final VisualizeTemplateRepository mVisualizeTemplateRepository;
    private final Profile mProfile;

    @Inject
    public GetVisualizeTemplateCase(PreExecutionThread preExecutionThread,
                                    PostExecutionThread postExecutionThread,
                                    VisualizeTemplateRepository visualizeTemplateRepository,
                                    Profile profile) {
        super(preExecutionThread, postExecutionThread);
        mVisualizeTemplateRepository = visualizeTemplateRepository;
        mProfile = profile;
    }

    @Override
    protected Observable<VisualizeTemplate> buildUseCaseObservable(Map<String, ?> clientParams) {
        return mVisualizeTemplateRepository.get(mProfile, clientParams);
    }
}
