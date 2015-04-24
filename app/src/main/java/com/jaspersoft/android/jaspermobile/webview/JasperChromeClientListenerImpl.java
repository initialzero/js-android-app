/*
 * Copyright © 2014 TIBCO Software, Inc. All rights reserved.
 * http://community.jaspersoft.com/project/jaspermobile-android
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is part of Jaspersoft Mobile for Android.
 *
 * Jaspersoft Mobile is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Jaspersoft Mobile is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Jaspersoft Mobile for Android. If not, see
 * <http://www.gnu.org/licenses/lgpl>.
 */

package com.jaspersoft.android.jaspermobile.webview;

import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.WebView;
import android.widget.ProgressBar;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class JasperChromeClientListenerImpl implements JasperChromeClientListener {
    private final List<ConsoleMessage> messages = new LinkedList<ConsoleMessage>();
    private final ProgressBar progressBar;

    public JasperChromeClientListenerImpl(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    @Override
    public void onProgressChanged(WebView view, int progress) {
        int maxProgress = progressBar.getMax();
        progressBar.setProgress((maxProgress / 100) * progress);
        if (progress == maxProgress) {
            progressBar.setVisibility(View.GONE);
        }
    }

    @Override
    public void onConsoleMessage(ConsoleMessage consoleMessage) {
        messages.add(consoleMessage);
    }

    public List<ConsoleMessage> getMessages() {
        return messages;
    }
}
