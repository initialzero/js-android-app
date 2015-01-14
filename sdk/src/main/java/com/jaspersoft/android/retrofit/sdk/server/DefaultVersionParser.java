package com.jaspersoft.android.retrofit.sdk.server;

import java.math.BigDecimal;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public final class DefaultVersionParser implements VersionParser {

    public static double getVersionCode(String version) {
        return new DefaultVersionParser().parse(version);
    }

    @Override
    public double parse(String version) {
        double versionCode = 0d;
        // update version code
        if (version != null) {
            String[] subs = version.split("\\.");

            BigDecimal decimalSubVersion, decimalFactor, decimalResult;
            BigDecimal decimalVersion = new BigDecimal("0");
            for (int i = 0; i < subs.length; i++) {
                try {
                    decimalSubVersion = new BigDecimal(Integer.parseInt(subs[i]));
                } catch (NumberFormatException ex) {
                    decimalSubVersion = new BigDecimal("0");
                }

                decimalFactor = new BigDecimal(String.valueOf(Math.pow(10, i * -1)));
                decimalResult = decimalSubVersion.multiply(decimalFactor);
                decimalVersion = decimalVersion.add(decimalResult);
            }
            versionCode = decimalVersion.doubleValue();
        }
        return versionCode;
    }
}