package com.jaspersoft.android.jaspermobile.presentation.contract;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface StartupContract {
    interface View {
    }

    interface ActionListener {
        void tryToSetupProfile();

        void setupNewProfile();
    }
}
