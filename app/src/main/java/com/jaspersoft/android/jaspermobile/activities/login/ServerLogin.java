package com.jaspersoft.android.jaspermobile.activities.login;

import com.google.inject.Inject;
import com.jaspersoft.android.jaspermobile.util.DefaultPrefHelper;
import com.jaspersoft.android.jaspermobile.util.GeneralPref_;
import com.jaspersoft.android.jaspermobile.util.ProfileHelper;
import com.jaspersoft.android.sdk.client.JsRestClient;
import com.jaspersoft.android.sdk.client.JsServerProfile;
import com.jaspersoft.android.sdk.client.oxm.report.FolderDataResponse;
import com.jaspersoft.android.sdk.client.oxm.server.ServerInfo;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;

/**
 * @author Andrew Tivodar
 * @since 1.9
 */
@EBean(scope = EBean.Scope.Singleton)
public class ServerLogin {

    @Inject
    JsRestClient jsRestClient;
    @Bean
    ProfileHelper profileHelper;
    @Pref
    GeneralPref_ generalPref;
    @Bean
    DefaultPrefHelper prefHelper;

    private LoginCallback listener;

    public void setListener(LoginCallback listener) {
        this.listener = listener;
    }

    @Background
    protected void userSignIn(String name, String password, String authTokenType) {
        JsRestClient tmpRestClient = new JsRestClient();
        tmpRestClient.setConnectTimeout(prefHelper.getConnectTimeoutValue());
        tmpRestClient.setReadTimeout(prefHelper.getReadTimeoutValue());
        // Need selected server profile
      //  tmpRestClient.setServerProfile(serverProfile);

        try {
            ServerInfo serverInfo = jsRestClient.getServerInfo();

            double currentVersion = serverInfo.getVersionCode();
            if (currentVersion < ServerInfo.VERSION_CODES.EMERALD_TWO) {
                listener.onInvalidServerVersion();
            }

            // Need selected server profile
          //  checkUserCredentials(jsServerProfile);
        } catch (RestClientException ex) {
            listener.onInvalidServerCredential();
        }
    }

    private void checkUserCredentials(JsServerProfile jsServerProfile) {
        try {
            String fullUri = jsServerProfile.getServerUrl()
                    + JsRestClient.REST_SERVICES_V2_URI + JsRestClient.REST_RESOURCES_URI;
            HttpHeaders headers = new HttpHeaders();
            headers.add("Accept", "application/repository.folder+xml");
            headers.add("Content-Type", "application/xml");
            HttpEntity<String> httpEntity = new HttpEntity<String>("", headers);

            ResponseEntity<FolderDataResponse> responseEntity = jsRestClient.getRestTemplate()
                    .exchange(fullUri, HttpMethod.GET, httpEntity, FolderDataResponse.class);
            String cookies = responseEntity.getHeaders().get("Cookie").get(0);
            // DO smt with cookies

            long profileId = jsServerProfile.getId();
            // Lets update ServerInfo snapshot for later use
            //profileHelper.updateCurrentInfoSnapshot(profileId, serverInfo);
            listener.onLogin();

        } catch (RestClientException ex) {
            listener.onInvalidUserCredential();
        }
    }


    public interface LoginCallback{
        public void onInvalidServerCredential();
        public void onInvalidServerVersion();
        public void onInvalidUserCredential();
        public void onLogin();
    }
}
