/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from TIBCO Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of TIBCO Jaspersoft Mobile for Android.
 *
 * TIBCO Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TIBCO Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with TIBCO Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.util;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.speech.RecognizerIntent;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Andrew Tivodar
 * @since 2.3
 */
public class VoiceRecognitionHelper {
    private static final String FIND_COMMAND = "find";
    private static final String RUN_COMMAND = "run";

    public static class VoiceCommand {
        public static final int FIND = 1;
        public static final int RUN = 2;
        public static final int UNDEFINED = 3;

        private int mCommand;
        private String mArgument;

        protected VoiceCommand(int mCommand, String mArgument) {
            if (mCommand == 0 || mArgument == null) throw new IllegalArgumentException();

            this.mCommand = mCommand;
            this.mArgument = mArgument;
        }

        public String getArgument() {
            return mArgument;
        }

        public int getCommand() {
            return mCommand;
        }
    }

    public static VoiceCommand parseCommand(ArrayList<String> matches) {
        for (String match : matches) {
            if (match.toLowerCase().startsWith(FIND_COMMAND)) {
                return new VoiceCommand(VoiceCommand.FIND, match.toLowerCase().substring(4));
            } else if (match.toLowerCase().startsWith(RUN_COMMAND)) {
                return new VoiceCommand(VoiceCommand.RUN, match.toLowerCase().substring(3));
            }
        }
        return new VoiceCommand(VoiceCommand.UNDEFINED, matches.get(0));
    }

    public static boolean isVoiceRecognizerAvailable(Context context) {
        if (context == null) {
            return false;
        }
        PackageManager packageManager = context.getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        return !activities.isEmpty();
    }
}
