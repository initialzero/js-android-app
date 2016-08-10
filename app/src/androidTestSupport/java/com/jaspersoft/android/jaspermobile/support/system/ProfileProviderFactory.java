package com.jaspersoft.android.jaspermobile.support.system;

/**
 * @author Tom Koptel
 * @since 2.6
 */
final class ProfileProviderFactory {

    private final CliArguments cliArguments;

    ProfileProviderFactory(CliArguments cliArguments) {
        this.cliArguments = cliArguments;
    }

    public static ProfileProviderFactory newInstance() {
        return new ProfileProviderFactory(CliArguments.newInstance());
    }

    public Provider<Profile> createCliProvider() {
        if (containsArguments()) {
            return CliProfileProvider.newInstance();
        }
        return new DefaultProfileProvider();
    }

    public Provider<Profile> createDefaultProvider() {
        return new DefaultProfileProvider();
    }

    public Provider<Profile> createFakeProvider(String alias) {
        return new DefaultProfileProvider(alias);
    }

    private boolean containsArguments() {
        boolean contains = false;
        for (String mandatoryArg : ProfileProvider.MANDATORY_ARGS) {
            contains |= cliArguments.contains(mandatoryArg);
        }
        return contains;
    }
}
