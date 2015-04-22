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
    private final Map<String, WeakReference<ParamHolder>> cache = new HashMap<String, WeakReference<ParamHolder>>();

    @Inject
    public ReportParamsStorage() {
    }

    @NonNull
    public ArrayList<ReportParameter> getReportParameters(@NonNull String resourceUri) {
        WeakReference<ParamHolder> weakReference = cache.get(resourceUri);
        ParamHolder holder = weakReference.get();
        if (holder == null) {
            return new ArrayList<ReportParameter>();
        } else {
            return holder.getReportParameters();
        }
    }

    @NonNull
    public ArrayList<InputControl> getInputControls(@NonNull String resourceUri) {
        WeakReference<ParamHolder> weakReference = cache.get(resourceUri);
        ParamHolder holder = weakReference.get();
        if (holder == null) {
            return new ArrayList<InputControl>();
        } else {
            return holder.getInputControls();
        }
    }

    public void putReportParameters(@NonNull String resourceUri, @NonNull ArrayList<ReportParameter> reportParameters) {
        ParamHolder holder = getHolder(resourceUri);
        holder.putReportParameters(reportParameters);
    }

    public void putInputControls(@NonNull String resourceUri, @NonNull ArrayList<InputControl> inputControls) {
        ParamHolder holder = getHolder(resourceUri);
        holder.putInputControls(inputControls);
    }

    private ParamHolder getHolder(@NonNull String resourceUri) {
        ParamHolder holder;
        WeakReference<ParamHolder> weakReference = cache.get(resourceUri);

        if (weakReference == null) {
            holder = putEmptyHolderInCache(resourceUri);
        } else {
            holder = weakReference.get();
            if (holder == null) {
                holder = putEmptyHolderInCache(resourceUri);
            }
        }

        return holder;
    }

    private ParamHolder putEmptyHolderInCache(String resourceUri) {
        ParamHolder holder = new ParamHolder();
        WeakReference<ParamHolder> weakReference = new WeakReference<ParamHolder>(holder);
        cache.put(resourceUri, weakReference);
        return holder;
    }

    private static class ParamHolder {
        private ArrayList<ReportParameter> reportParameters;
        private ArrayList<InputControl> inputControls;

        public ArrayList<ReportParameter> getReportParameters() {
            return reportParameters;
        }

        public void putReportParameters(ArrayList<ReportParameter> reportParameters) {
            if (reportParameters == null) {
                throw new IllegalArgumentException("Report parameters should not be null");
            }
            if (this.reportParameters == null) {
                this.reportParameters = reportParameters;
            } else {
                this.reportParameters.clear();
                this.reportParameters.addAll(reportParameters);
            }
        }

        public ArrayList<InputControl> getInputControls() {
            return inputControls;
        }

        public void putInputControls(ArrayList<InputControl> inputControls) {
            if (inputControls == null) {
                throw new IllegalArgumentException("Input controls should not be null");
            }
            if (this.inputControls == null) {
                this.inputControls = inputControls;
            } else {
                this.inputControls.clear();
                this.inputControls.addAll(inputControls);
            }
        }
    }
}
