package com.jaspersoft.android.jaspermobile.util;

import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Tivodar
 * @since 2.0
 */
public class ReportParamsHolder {
    public static Map<String, WeakReference<ArrayList<InputControl>>> inputControls = new HashMap<String, WeakReference<ArrayList<InputControl>>>();
    public static Map<String, WeakReference<ArrayList<ReportParameter>>> reportParams = new HashMap<String, WeakReference<ArrayList<ReportParameter>>>();
}
