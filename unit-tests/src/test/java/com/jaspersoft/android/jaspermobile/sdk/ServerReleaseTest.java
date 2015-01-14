package com.jaspersoft.android.jaspermobile.sdk;

import com.jaspersoft.android.jaspermobile.test.support.UnitTestSpecification;
import com.jaspersoft.android.retrofit.sdk.server.ServerRelease;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class ServerReleaseTest extends UnitTestSpecification {
    @Test
    public void shouldParseSemanticVersioning() {
        Map<String, ServerRelease> doubleMap = new HashMap<String, ServerRelease>();
        doubleMap.put("5.0.0", ServerRelease.EMERALD);
        doubleMap.put("5.2.0", ServerRelease.EMERALD_MR1);
        doubleMap.put("5.5.0", ServerRelease.EMERALD_MR2);
        doubleMap.put("5.6.0", ServerRelease.EMERALD_MR3);
        doubleMap.put("6.0", ServerRelease.AMBER);
        doubleMap.put("20.0", ServerRelease.UNKNOWN);

        for (Map.Entry<String, ServerRelease> entry : doubleMap.entrySet()) {
            assertThat(ServerRelease.parseString(entry.getKey()), is(entry.getValue())) ;
        }
    }

    @Test
    public void shouldParseNonSemanticVersioning() {
        String[] nonSemanticOne = {"5.6.0 Preview", "5.6.0-BETA"};
        for (String nonSemanticVersion : nonSemanticOne) {
            assertThat(ServerRelease.parseString(nonSemanticVersion), is(ServerRelease.EMERALD_MR3));
        }
    }
}
