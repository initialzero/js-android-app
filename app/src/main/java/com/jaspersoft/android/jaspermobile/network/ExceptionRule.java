package com.jaspersoft.android.jaspermobile.network;


import com.google.common.collect.Maps;
import com.jaspersoft.android.jaspermobile.R;

import org.springframework.http.HttpStatus;

import java.util.EnumMap;

/**
 * @author Tom Koptel
 * @since 1.9
 */
public enum ExceptionRule {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, R.string.error_http_400),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, R.string.error_http_401),
    FORBIDDEN(HttpStatus.FORBIDDEN, R.string.error_http_403),
    NOT_FOUND(HttpStatus.FORBIDDEN, R.string.error_http_404),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, R.string.error_http_500),
    BAD_GATEWAY(HttpStatus.BAD_GATEWAY, R.string.error_http_502),
    SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, R.string.error_http_503),
    GATEWAY_TIMEOUT(HttpStatus.GATEWAY_TIMEOUT, R.string.error_http_504);

    private final HttpStatus httpStatus;
    private final int message;

    ExceptionRule(HttpStatus httpStatus, int message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public int getMessage() {
        return message;
    }

    public static EnumMap<HttpStatus, ExceptionRule> all() {
        EnumMap<HttpStatus, ExceptionRule> collection =
                Maps.newEnumMap(HttpStatus.class);
        for (ExceptionRule rule : values()) {
            collection.put(rule.getHttpStatus(), rule);
        }
        return collection;
    }
}
