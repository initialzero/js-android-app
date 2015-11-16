package com.jaspersoft.android.jaspermobile.presentation.view;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface AuthenticationView extends LoadDataView {
    void showAliasDuplicateError();
    void showAliasReservedError();
    void showAliasRequiredError();
    void showServerUrlFormatError();
    void showServerUrlRequiredError();
    void showUsernameRequiredError();
    void showPasswordRequiredError();
    void showServerVersionNotSupported();
    void showCredentialsError();
    void showFailedToAddProfile(String message);
    void navigateToApp();
}
