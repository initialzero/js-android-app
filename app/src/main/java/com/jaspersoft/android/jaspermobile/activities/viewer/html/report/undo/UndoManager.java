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

package com.jaspersoft.android.jaspermobile.activities.viewer.html.report.undo;

import android.support.annotation.Nullable;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class UndoManager<T> {
    private final ArrayDeque<T> undoStack;
    private final Set<T> set = new LinkedHashSet<T>();

    public UndoManager() {
        undoStack = new ArrayDeque<T>();
    }

    public UndoManager(Collection<T> commands) {
        undoStack = new ArrayDeque<T>(commands);
    }

    public void add(T command) {
        set.add(command);
        undoStack.clear();
        undoStack.addAll(set);
    }

    public Queue<T> getUndoStack() {
        return undoStack;
    }

    @Nullable
    public T undo() {
        return undoStack.pollLast();
    }

    @Nullable
    public T peekLatest() {
        return undoStack.peekLast();
    }
}
