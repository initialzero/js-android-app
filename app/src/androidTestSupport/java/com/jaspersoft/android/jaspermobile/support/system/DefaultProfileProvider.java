package com.jaspersoft.android.jaspermobile.support.system;

/**
 * @author Tom Koptel
 * @since 2.6
 */
final class DefaultProfileProvider extends ProfileProvider {
    private final String alias;

    public DefaultProfileProvider() {
        this(TEST_PROFILE_ALIAS);
    }

    public DefaultProfileProvider(String alias) {
        this.alias = alias;
    }

    @Override
    public Profile provide() {
        return new Profile.Builder()
                .alias(alias)
                .username("phoneuser")
                .password("phoneuser")
                .url("http://mobiledemo.jaspersoft.com/jasperserver-pro/")
                .version("6.3")
                .edition("PRO")
                .build();
    }
}
