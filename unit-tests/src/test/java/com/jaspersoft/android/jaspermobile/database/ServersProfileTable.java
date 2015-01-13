package com.jaspersoft.android.jaspermobile.database;

import android.net.Uri;

import com.jaspersoft.android.jaspermobile.db.MobileDbProvider;
import com.jaspersoft.android.jaspermobile.db.model.ServerProfiles;
import com.jaspersoft.android.jaspermobile.test.support.DatabaseSpecification;

import org.junit.Test;

import static com.jaspersoft.android.jaspermobile.util.JsAssertions.assertNewUri;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ServersProfileTable extends DatabaseSpecification {
    @Test
    public void testAliasFieldShouldBeUniqueness() {
        ServerProfiles profile = new ServerProfiles();
        profile.setAlias("alias");
        profile.setServerUrl("http://example.com");
        Uri newUri = getContentResolver().insert(
                MobileDbProvider.SERVER_PROFILES_CONTENT_URI, profile.getContentValues());
        assertNewUri(newUri);

        profile = new ServerProfiles();
        profile.setAlias("alias");
        profile.setServerUrl("http://example/1.com");
        newUri = getContentResolver().insert(
                MobileDbProvider.SERVER_PROFILES_CONTENT_URI, profile.getContentValues());
        assertThat(newUri, nullValue());
    }

    @Test
    public void testAliasFieldShouldNotBeNull() {
        ServerProfiles profile = new ServerProfiles();
        profile.setAlias(null);
        profile.setServerUrl("http://example.com");
        Uri newUri = getContentResolver().insert(
                MobileDbProvider.SERVER_PROFILES_CONTENT_URI, profile.getContentValues());
        assertThat(newUri, nullValue());
    }

    @Test
    public void testServerUrlShouldNotBeNull() {
        ServerProfiles profile = new ServerProfiles();
        profile.setAlias("alias");
        profile.setServerUrl(null);
        Uri newUri = getContentResolver().insert(
                MobileDbProvider.SERVER_PROFILES_CONTENT_URI, profile.getContentValues());
        assertThat(newUri, nullValue());
    }
}
