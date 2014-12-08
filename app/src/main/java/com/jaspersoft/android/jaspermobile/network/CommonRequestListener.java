package com.jaspersoft.android.jaspermobile.network;

import android.app.Activity;

import com.octo.android.robospice.persistence.exception.SpiceException;

import org.springframework.http.HttpStatus;

import java.util.EnumMap;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public abstract class CommonRequestListener<T> extends SimpleRequestListener<T> {

    private final EnumMap<HttpStatus, ExceptionRule> rules;

    protected CommonRequestListener() {
        this.rules = ExceptionRule.all();
    }

    public CommonRequestListener(EnumMap<HttpStatus, ExceptionRule> rules) {
        this.rules = rules;
    }

    public void addRule(ExceptionRule rule) {
        rules.put(rule.getHttpStatus(), rule);
    }

    public void removeRule(ExceptionRule rule) {
        rules.remove(rule.getHttpStatus());
    }

    protected HttpStatus extractStatusCode(SpiceException spiceException) {
        return RequestExceptionHandler.extractStatusCode(spiceException);
    }

    @Override
    public final void onRequestFailure(SpiceException spiceException) {
        RequestExceptionHandler.handle(spiceException, getCurrentActivity(), rules);
        onSemanticFailure(spiceException);
    }

    @Override
    public final void onRequestSuccess(T data) {
        onSemanticSuccess(data);
    }

    public void onSemanticFailure(SpiceException spiceException) {
        // Override method for additional implementation
    }

    public void onSemanticSuccess(T data) {
        // Override method for additional implementation
    }

    public EnumMap<HttpStatus, ExceptionRule> getRules() {
        return rules;
    }

    public abstract Activity getCurrentActivity();

}
