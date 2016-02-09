package com.jaspersoft.android.jaspermobile.presentation.contract;

import com.jaspersoft.android.jaspermobile.presentation.model.ProfileViewModel;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface NavigationContract {
    interface View {
        void toggleRecentlyViewedNavigation(boolean visibility);
        void showActiveProfile(ProfileViewModel activeProfile);
        void showProfiles(List<ProfileViewModel> profiles);
    }

    interface ActionListener {
        void loadProfiles();
        void loadActiveProfile();
        void activateProfile(ProfileViewModel profile);
    }
}
