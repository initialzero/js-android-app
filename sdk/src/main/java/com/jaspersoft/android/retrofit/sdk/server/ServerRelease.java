package com.jaspersoft.android.retrofit.sdk.server;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public enum ServerRelease {
    UNKNOWN(0d),
    EMERALD(5.0d),
    EMERALD_MR1(5.2d),
    EMERALD_MR2(5.5d),
    EMERALD_MR3(5.6d),
    AMBER(6.0d);

    private final double mVersionCode;

    ServerRelease(double versionCode) {
        this.mVersionCode = versionCode;
    }

    public double code() {
        return mVersionCode;
    }

    public static ServerRelease parseString(String versionName) {
        return parseString(versionName, new DefaultVersionParser());
    }

    public static ServerRelease parseString(String versionName, VersionParser parser) {
        if (versionName == null) {
            throw new IllegalArgumentException("Argument 'versionName' should not be null");
        }
        double versionCode = parser.parse(versionName);
        return getByVersionCode(versionCode);
    }

    public static ServerRelease getByVersionCode(final double versionCode) {
        for (ServerRelease release : ServerRelease.values()) {
            if (Double.compare(release.code(), versionCode) == 0) {
                return release;
            }
        }
        return UNKNOWN;
    }
}