package com.jaspersoft.android.jaspermobile.presentation.view;

/**
 * @author Tom Koptel
 * @since 2.3
 */
public interface LoadDataView {
    void showLoading();
    void hideLoading();
    void showRetry();
    void hideRetry();
    void showError(String message);
}
