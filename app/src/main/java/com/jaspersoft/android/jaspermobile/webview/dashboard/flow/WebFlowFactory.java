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

package com.jaspersoft.android.jaspermobile.webview.dashboard.flow;

import com.jaspersoft.android.jaspermobile.domain.JasperServer;
import com.jaspersoft.android.sdk.client.oxm.resource.ResourceLookup;
import com.jaspersoft.android.sdk.service.data.server.ServerVersion;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public final class WebFlowFactory {
    private final JasperServer mServer;

    public WebFlowFactory(JasperServer server) {
        mServer = server;
    }

    public WebFlowStrategy createFlow(ResourceLookup resource) {
        ServerVersion serverVersion = ServerVersion.valueOf(mServer.getVersion());
        return createFlow(serverVersion, resource);
    }

    /**
     * Creates most appropriate strategy
     *
     * @param version represents double string to identify current JRS version
     * @return specific web flow strategy which varies between JRS releases
     */
    public WebFlowStrategy createFlow(ServerVersion version, ResourceLookup resource) {
        WebFlow webFlow;

        if (resource.getResourceType() == ResourceLookup.ResourceType.legacyDashboard) {
            webFlow = new EmeraldWebFlow();
        } else {
            if (version.lessThanOrEquals(ServerVersion.v5_6_1)) {
                webFlow = new EmeraldWebFlow();
            } else {
                webFlow = new AmberWebFlow();
            }
        }

        return new WebFlowStrategyImpl(mServer, webFlow, resource);
    }
}
