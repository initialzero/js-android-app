package com.jaspersoft.android.jaspermobile.util;

import android.support.annotation.NonNull;

import com.google.inject.Inject;
import com.jaspersoft.android.sdk.client.oxm.control.InputControl;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Andrew Tivodar
 * @author Tom Koptel
 * @since 2.0
 */
public class ReportParamsStorage {
    private final Map<String, WeakReference<ArrayList<ReportParameter>>> paramsCache = new HashMap<String, WeakReference<ArrayList<ReportParameter>>>();
    private final Map<String, WeakReference<ArrayList<InputControl>>> inputControlCache = new HashMap<String, WeakReference<ArrayList<InputControl>>>();

    @Inject
    public ReportParamsStorage() {
    }

    @NonNull
    public ArrayList<ReportParameter> getReportParameters(@NonNull String resourceUri) {
        WeakReference<ArrayList<ReportParameter>> weakReference = paramsCache.get(resourceUri);
        ArrayList<ReportParameter> params = weakReference.get();
        if (params == null) {
            return new ArrayList<ReportParameter>();
        } else {
            return params;
        }
    }

    @NonNull
    public ArrayList<InputControl> getInputControls(@NonNull String resourceUri) {
        WeakReference<ArrayList<InputControl>> weakReference = inputControlCache.get(resourceUri);
        ArrayList<InputControl> controls = weakReference.get();
        if (controls == null) {
            return new ArrayList<InputControl>();
        } else {
            return controls;
        }
    }

    public void putReportParameters(@NonNull String resourceUri, @NonNull ArrayList<ReportParameter> reportParameters) {
        WeakReference<ArrayList<ReportParameter>> weakReference = paramsCache.get(resourceUri);
        if (weakReference == null) {
            weakReference = new WeakReference<ArrayList<ReportParameter>>(reportParameters);
            paramsCache.put(resourceUri, weakReference);
        } else {
            if (weakReference.get() == null) {
                throw new IllegalStateException("Cache for report parameters was GC");
            } else {
                weakReference.get().clear();
                weakReference.get().addAll(reportParameters);
            }
        }
    }

    public void putInputControls(@NonNull String resourceUri, @NonNull ArrayList<InputControl> inputControls) {
        WeakReference<ArrayList<InputControl>> weakReference = inputControlCache.get(resourceUri);
        if (weakReference == null) {
            weakReference = new WeakReference<ArrayList<InputControl>>(inputControls);
            inputControlCache.put(resourceUri, weakReference);
        } else {
            if (weakReference.get() == null) {
                throw new IllegalStateException("Cache for report parameters was GC");
            } else {
                weakReference.get().clear();
                weakReference.get().addAll(inputControls);
            }
        }
    }
}
