package com.jaspersoft.android.jaspermobile.data.cache.report;

import com.jaspersoft.android.jaspermobile.internal.di.PerProfile;
import com.jaspersoft.android.jaspermobile.util.ReportParamsStorage;
import com.jaspersoft.android.sdk.client.oxm.report.ReportParameter;

import java.util.List;

import javax.inject.Inject;

/**
 * @author Tom Koptel
 * @since 2.3
 */
@PerProfile
public final class InMemoryReportParamsCache implements ReportParamsCache {
    private final ReportParamsStorage mParamsStorage;

    @Inject
    public InMemoryReportParamsCache(ReportParamsStorage paramsStorage) {
        mParamsStorage = paramsStorage;
    }

    @Override
    public void put(String uri, List<ReportParameter> parameters) {
        mParamsStorage.getInputControlHolder(uri).setReportParams(parameters);
    }

    @Override
    public List<ReportParameter> get(String uri) {
        return mParamsStorage.getInputControlHolder(uri).getReportParams();
    }

    @Override
    public void evict(String uri) {
        mParamsStorage.clearInputControlHolder(uri);
    }

    @Override
    public boolean contains(String uri) {
        List<ReportParameter> reportParams = mParamsStorage.getInputControlHolder(uri).getReportParams();
        return reportParams != null && !reportParams.isEmpty();
    }
}
