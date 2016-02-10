package com.jaspersoft.android.jaspermobile.presentation.contract;

import com.jaspersoft.android.jaspermobile.domain.Profile;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface StartupContract {
    interface View {
    }

    interface ActionListener {
        void tryToSetupProfile();

        void setupNewProfile(Profile profile);
    }
}
