package com.jaspersoft.android.jaspermobile.support.system;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;

/**
 * @author Tom Koptel
 * @since 2.6
 */
final class AppProfileRegistry {
    private static final String PREF_NAME = "JasperAccountManager";
    private static final String ACCOUNT_NAME_KEY = "ACCOUNT_NAME_KEY";

    private final AccountManager accountManager;
    private final ProfileMapper profileMapper;
    private final ProfilePasswordStoreAction profilePasswordStoreAction;
    private final SharedPreferences activeCache;

    AppProfileRegistry(
            Context context,
            ProfileMapper profileMapper,
            ProfilePasswordStoreAction profilePasswordStoreAction
    ) {
        this.accountManager = AccountManager.get(context);
        this.profileMapper = profileMapper;
        this.profilePasswordStoreAction = profilePasswordStoreAction;
        this.activeCache = context.getSharedPreferences(PREF_NAME, Activity.MODE_PRIVATE);
    }

    public static AppProfileRegistry newInstance() {
        Context context = InstrumentationRegistry.getContext();
        ProfileMapper profileMapper = new ProfileMapper();
        ProfilePasswordStoreAction profilePasswordStoreAction = new ProfilePasswordStoreAction(context, profileMapper);

        return new AppProfileRegistry(context, profileMapper, profilePasswordStoreAction);
    }

    public void register(Profile profile) throws ProfileRegistryException {
        boolean accountRegistered = registerSystemAccount(profile);
        if (accountRegistered) {
            registerPassword(profile);
            activateProfile(profile);
        } else {
            throw new ProfileExistsException();
        }
    }

    private void activateProfile(Profile profile) {
        activeCache.edit().putString(ACCOUNT_NAME_KEY, profile.getAlias()).apply();
    }

    public void unregister(Profile profile) {
        Account account = profileMapper.toAccount(profile);
        accountManager.removeAccountExplicitly(account);
    }

    public void removeAll() {
        Account[] accountsByType = accountManager.getAccountsByType(ProfileMapper.ACCOUNT_TYPE);
        for (Account account : accountsByType) {
            accountManager.removeAccountExplicitly(account);
        }
    }

    private boolean registerSystemAccount(Profile profile) {
        Account account = profileMapper.toAccount(profile);
        boolean added = accountManager.addAccountExplicitly(account, null, null);
        if (added) {
            accountManager.setUserData(account, "ALIAS_KEY", profile.getAlias());
            accountManager.setUserData(account, "SERVER_URL_KEY", profile.getUrl());
            accountManager.setUserData(account, "ORGANIZATION_KEY", profile.getOrganization());
            accountManager.setUserData(account, "USERNAME_KEY", profile.getUsername());
            accountManager.setUserData(account, "EDITION_KEY", profile.getEdition());
            accountManager.setUserData(account, "VERSION_NAME_KEY", profile.getVersion());
        }
        return added;
    }

    private void registerPassword(Profile profile) {
        boolean passwordSaved = profilePasswordStoreAction.perform(profile);

        if (!passwordSaved) {
            throw new ProfileRegistryException("Failed to save password for profile: " + profile);
        }
    }

    public static class ProfileExistsException extends RuntimeException {
    }
    public static class ProfileRegistryException extends RuntimeException {
        public ProfileRegistryException(String detailMessage) {
            super(detailMessage);
        }
    }
}
