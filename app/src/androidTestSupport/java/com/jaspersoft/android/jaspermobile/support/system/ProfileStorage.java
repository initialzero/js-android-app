package com.jaspersoft.android.jaspermobile.support.system;

/**
 * @author Tom Koptel
 * @since 2.6
 */
public final class ProfileStorage {

    private final ProfileProviderFactory profileProviderFactory;
    private final AppProfileRegistry appProfileRegistry;


    public ProfileStorage() {
        this(
                ProfileProviderFactory.newInstance(),
                AppProfileRegistry.newInstance()
        );
    }

    ProfileStorage(
            ProfileProviderFactory profileProviderFactory,
            AppProfileRegistry profileRegistry
    ) {
        this.profileProviderFactory = profileProviderFactory;
        this.appProfileRegistry = profileRegistry;
    }

    public void createProfile(String alias) {
        Provider<Profile> fakeProfile = profileProviderFactory.createFakeProvider(alias);
        register(fakeProfile);
    }

    public void createCliProfile() {
        Provider<Profile> cliProvider = profileProviderFactory.createCliProvider();
        register(cliProvider);
    }

    public void createMobileDemoProfile() {
        Provider<Profile> defaultProvider = profileProviderFactory.createDefaultProvider();
        register(defaultProvider);
    }

    private void register(Provider<Profile> provider) {
        Profile profile = provider.provide();

        try {
            appProfileRegistry.register(profile);
        } catch (AppProfileRegistry.ProfileExistsException ex) {
            appProfileRegistry.unregister(profile);
            appProfileRegistry.register(profile);
        }
    }

    public void removeAll() {
        appProfileRegistry.removeAll();
    }
}
