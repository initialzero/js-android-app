package com.jaspersoft.android.jaspermobile.support.rule;

import com.jaspersoft.android.jaspermobile.support.system.ProfileStorage;

import org.junit.rules.ExternalResource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.6
 */
public final class AuthenticateProfileTestRule extends ExternalResource {
    private final List<String> aliases;
    private final ProfileStorage profileStorage;

    private AuthenticateProfileTestRule(Builder builder, ProfileStorage profileStorage) {
        this.aliases = Collections.unmodifiableList(builder.aliases);
        this.profileStorage = profileStorage;
    }

    public static AuthenticateProfileTestRule create() {
        return new Builder().done();
    }

    public static Builder configure() {
        return new Builder();
    }

    @Override
    protected void before() throws Throwable {
        super.before();

        profileStorage.createCliProfile();

        for (String alias : aliases) {
            profileStorage.createProfile(alias);
        }
    }

    @Override
    protected void after() {
        super.after();
        profileStorage.removeAll();
    }

    public static class Builder {
        private List<String> aliases = new ArrayList<>(10);

        public Builder withFakeProfile(String alias) {
            aliases.add(alias);
            return this;
        }

        public AuthenticateProfileTestRule done() {
            ProfileStorage profileStorage = new ProfileStorage();
            return new AuthenticateProfileTestRule(this, profileStorage);
        }
    }
}
