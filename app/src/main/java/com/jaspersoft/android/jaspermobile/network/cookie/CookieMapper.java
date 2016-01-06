package com.jaspersoft.android.jaspermobile.network.cookie;


import android.support.annotation.Nullable;

import org.apache.http.impl.cookie.BasicClientCookie;

import java.net.HttpCookie;
import java.util.Date;

/**
 * @author Tom Koptel
 * @since 2.3
 */
class CookieMapper {

    @Nullable
    public org.apache.http.cookie.Cookie toApacheCookie(@Nullable HttpCookie cookie) {
        if (cookie == null) {
            return null;
        }

        BasicClientCookie clientCookie = new BasicClientCookie(cookie.getName(), cookie.getValue());
        clientCookie.setDomain(cookie.getDomain());
        clientCookie.setPath(cookie.getPath());
        clientCookie.setVersion(cookie.getVersion());

        Date expiryDate = new Date(new Date().getTime() + cookie.getMaxAge());
        clientCookie.setExpiryDate(expiryDate);

        return clientCookie;
    }
}
