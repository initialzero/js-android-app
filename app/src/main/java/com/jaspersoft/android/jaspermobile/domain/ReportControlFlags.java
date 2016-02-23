package com.jaspersoft.android.jaspermobile.domain;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public class ReportControlFlags {
    private final boolean mNeedPrompt;
    private final boolean mHasControls;

    public ReportControlFlags(boolean alwaysPrompt, boolean hasControls) {
        mNeedPrompt = alwaysPrompt;
        mHasControls = hasControls;
    }

    public boolean hasControls() {
        return mHasControls;
    }

    public boolean needPrompt() {
        return mNeedPrompt;
    }
}
