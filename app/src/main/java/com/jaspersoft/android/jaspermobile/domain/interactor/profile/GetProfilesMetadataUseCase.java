/*
 * Copyright © 2016 TIBCO Software,Inc.All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software:you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation,either version 3of the License,or
 * (at your option)any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android.If not,see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.domain.interactor.profile;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.jaspermobile.domain.Profile;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadata;
import com.jaspersoft.android.jaspermobile.domain.ProfileMetadataCollection;
import com.jaspersoft.android.jaspermobile.domain.executor.PostExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.executor.PreExecutionThread;
import com.jaspersoft.android.jaspermobile.domain.interactor.AbstractSimpleUseCase;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.JasperServerRepository;
import com.jaspersoft.android.jaspermobile.domain.repository.profile.ProfileRepository;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func1;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class GetProfilesMetadataUseCase extends AbstractSimpleUseCase<ProfileMetadataCollection> {
    private final ProfileRepository mProfileRepository;
    private final JasperServerRepository mServerRepository;

    @Inject
    public GetProfilesMetadataUseCase(
            PreExecutionThread preExecutionThread,
            PostExecutionThread postExecutionThread,
            ProfileRepository profileRepository,
            JasperServerRepository serverRepository
    ) {
        super(preExecutionThread, postExecutionThread);
        mProfileRepository = profileRepository;
        mServerRepository = serverRepository;
    }

    @Override
    protected Observable<ProfileMetadataCollection> buildUseCaseObservable() {
        Observable<List<Profile>> listProfiles = mProfileRepository.listProfiles();
        return listProfiles.flatMap(new Func1<List<Profile>, Observable<ProfileMetadataCollection>>() {
            @Override
            public Observable<ProfileMetadataCollection> call(List<Profile> profiles) {
                Profile activeProfile = mProfileRepository.getActiveProfile();
                List<ProfileMetadata> metadataList = new ArrayList<>(profiles.size());
                for (Profile profile : profiles) {
                    boolean isActive = profile.equals(activeProfile);
                    JasperServer server = mServerRepository.getServer(profile);
                    ProfileMetadata metadata = new ProfileMetadata(profile, server, isActive);
                    metadataList.add(metadata);
                }

                ProfileMetadataCollection profileMetadataCollection =
                        new ProfileMetadataCollection(metadataList);
                return Observable.just(profileMetadataCollection);
            }
        });
    }
}
