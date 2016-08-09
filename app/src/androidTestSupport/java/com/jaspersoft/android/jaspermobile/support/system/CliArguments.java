package com.jaspersoft.android.jaspermobile.support.system;

import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

/**
 * @author Tom Koptel
 * @since 2.6
 */
final class CliArguments {

    private final Bundle args;

    CliArguments(Bundle args) {
        this.args = args;
    }

    public static CliArguments newInstance() {
        return new CliArguments(InstrumentationRegistry.getArguments());
    }

    public String getArgument(String argument) {
        return args.getString(argument);
    }

    public boolean contains(String argument) {
        return args.containsKey(argument);
    }
}
