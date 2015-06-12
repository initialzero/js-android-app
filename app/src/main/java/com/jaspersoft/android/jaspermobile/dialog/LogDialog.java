/*
 * Copyright © 2015 TIBCO Software, Inc. All rights reserved.
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

package com.jaspersoft.android.jaspermobile.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ConsoleMessage;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * @author Tom Koptel
 * @since 2.0
 */
public class LogDialog extends DialogFragment {
    private List<ConsoleMessage> messages;

    public void setMessages(List<ConsoleMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Log").setAdapter(new Adapter(), null);
        return builder.create();
    }

    public static void create(FragmentManager manager, List<ConsoleMessage> messages) {
        LogDialog logDialog = new LogDialog();
        logDialog.setMessages(messages);
        logDialog.show(manager, null);
    }

    private class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public ConsoleMessage getItem(int position) {
            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Context context = parent.getContext();
            ViewHolder viewHolder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(parent.getContext());
                convertView = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            ConsoleMessage message = getItem(position);
            viewHolder.text.setText(message.message());
            viewHolder.text.setTextColor(getLogCollor(context, message));
            viewHolder.subText.setText(String.format("Line number: %d\nSource id: %s", message.lineNumber(), message.sourceId()));

            return convertView;
        }

        public int getLogCollor(Context context, ConsoleMessage message) {
            Resources resources = context.getResources();
            switch (message.messageLevel()) {
                case ERROR:
                    return resources.getColor(android.R.color.holo_red_dark);
                case WARNING:
                    return resources.getColor(android.R.color.holo_red_light);
                case DEBUG:
                    return resources.getColor(android.R.color.holo_orange_dark);
                case LOG:
                    return resources.getColor(android.R.color.white);
                default:
                    return 0;
            }
        }
    }

    private static class ViewHolder {
        TextView text;
        TextView subText;

        ViewHolder(View view) {
            text = (TextView) view.findViewById(android.R.id.text1);
            subText = (TextView) view.findViewById(android.R.id.text2);
        }
    }
}
