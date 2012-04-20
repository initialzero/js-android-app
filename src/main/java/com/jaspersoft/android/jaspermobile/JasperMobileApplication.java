/*
 * Copyright (C) 2005 - 2012 Jaspersoft Corporation. All rights reserved.
 * http://www.jaspersoft.com.
 *
 * Unless you have purchased a commercial license agreement from Jaspersoft,
 * the following license terms apply:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.jaspersoft.android.jaspermobile;

import com.google.inject.Module;
import roboguice.application.RoboApplication;

import java.util.List;

/**
 * @author Ivan Gadzhega
 * @version $Id$
 * @since 1.0
 */
public class JasperMobileApplication extends RoboApplication {

    // Report output cache directory name
    public static final String REPORT_OUTPUT_DIR_NAME = "report.output";

    @Override
    protected void addApplicationModules(List<Module> modules) {
        modules.add(new JasperMobileModule());
    }
}
