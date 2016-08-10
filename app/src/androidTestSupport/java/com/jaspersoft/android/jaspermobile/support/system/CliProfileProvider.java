package com.jaspersoft.android.jaspermobile.support.system;

import android.support.annotation.Nullable;

/**
 * Extracts Profile from CLI configuration or provides default solution.
 *
 * @author Tom Koptel
 * @since 2.6
 */
final class CliProfileProvider extends ProfileProvider {
    private final CliArguments cliArguments;

    CliProfileProvider(CliArguments cliArguments) {
        this.cliArguments = cliArguments;
    }

    public static CliProfileProvider newInstance() {
        return new CliProfileProvider(CliArguments.newInstance());
    }

    @Nullable
    public Profile provide() {
        validateArguments();
        return extractCredentials();
    }

    private void validateArguments() throws MissingCredentialException {
        for (String mandatoryArg : MANDATORY_ARGS) {
            checkMandatoryArgument(mandatoryArg);
        }
    }

    private Profile extractCredentials() {
        String alias = cliArguments.getArgument(ALIAS_ARG);

        String username = cliArguments.getArgument(USERNAME_ARG);
        String password = cliArguments.getArgument(PASSWORD_ARG);
        String organization = cliArguments.getArgument(ORGANIZATION_ARG);

        String serverUrl = cliArguments.getArgument(SERVER_URL_ARG);
        String version = cliArguments.getArgument(VERSION_ARG);
        String edition = cliArguments.getArgument(EDITION_ARG);

        return new Profile.Builder()
                .alias(alias)
                .username(username)
                .password(password)
                .url(serverUrl)
                .organization(organization)
                .version(version)
                .edition(edition)
                .build();
    }

    private void checkMandatoryArgument(String arg) {
        if (!cliArguments.contains(arg)) {
            throw new MissingCredentialException("Missing argument '" + arg +
                    "'. Please supply argument as -Pandroid.testInstrumentationRunnerArguments." + arg + "=value");
        }
    }

    public static class MissingCredentialException extends RuntimeException {
        MissingCredentialException(String detailMessage) {
            super(detailMessage);
        }
    }
}
