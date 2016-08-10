package com.jaspersoft.android.jaspermobile.support.system;

import android.accounts.Account;
import android.os.Bundle;

/**
 * @author Tom Koptel
 * @since 2.6
 */
final class ProfileMapper {
    static final String ACCOUNT_TYPE = "com.jaspersoft";

    private static final String ALIAS_KEY = "ALIAS_KEY";
    private static final String SERVER_URL_KEY = "SERVER_URL_KEY";
    private static final String EDITION_KEY = "EDITION_KEY";
    private static final String VERSION_NAME_KEY = "VERSION_NAME_KEY";
    private static final String ORGANIZATION_KEY = "ORGANIZATION_KEY";
    private static final String USERNAME_KEY = "USERNAME_KEY";

    public Bundle toBundle(Profile profile) {
        Bundle bundle = new Bundle();
        bundle.putString(ALIAS_KEY, profile.getAlias());

        bundle.putString(USERNAME_KEY, profile.getUsername());
        bundle.putString(ORGANIZATION_KEY, profile.getOrganization());

        bundle.putString(EDITION_KEY, profile.getEdition());
        bundle.putString(VERSION_NAME_KEY, profile.getVersion());
        bundle.putString(SERVER_URL_KEY, profile.getUrl());

        return bundle;
    }

    public Account toAccount(Profile profile) {
        return new Account(profile.getAlias(), ACCOUNT_TYPE);
    }
}
