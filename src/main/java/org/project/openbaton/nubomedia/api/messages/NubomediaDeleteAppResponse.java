package org.project.openbaton.nubomedia.api.messages;


import java.util.UUID;

/**
 * Created by maa on 09.10.15.
 */
public class NubomediaDeleteAppResponse {

    private String appID;
    private String appName;
    private int code;

    public NubomediaDeleteAppResponse() {
    }

    public NubomediaDeleteAppResponse(String appID, String appName, int deleteStatus) {
        this.appID = appID;
        this.appName = appName;
        this.code = deleteStatus;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
