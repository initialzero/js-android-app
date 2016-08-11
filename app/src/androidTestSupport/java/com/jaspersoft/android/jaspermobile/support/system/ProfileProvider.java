package com.jaspersoft.android.jaspermobile.support.system;

/**
 * @author Tom Koptel
 * @since 2.6
 */
abstract class ProfileProvider implements Provider<Profile> {
    static String TEST_PROFILE_ALIAS = "Demo PhoneUser";

    static String ALIAS_ARG = "alias";
    static String USERNAME_ARG = "username";
    static String PASSWORD_ARG = "password";
    static String SERVER_URL_ARG = "serverUrl";
    static String ORGANIZATION_ARG = "organization";
    static String VERSION_ARG = "version";
    static String EDITION_ARG = "edition";

    static String[] MANDATORY_ARGS = {ALIAS_ARG, USERNAME_ARG,
            PASSWORD_ARG, SERVER_URL_ARG, VERSION_ARG, EDITION_ARG};
}
