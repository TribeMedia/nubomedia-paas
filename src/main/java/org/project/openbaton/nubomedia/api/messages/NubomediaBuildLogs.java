package org.project.openbaton.nubomedia.api.messages;

/**
 * Created by maa on 12.10.15.
 */
public class NubomediaBuildLogs {

    private String id;
    private String appName;
    private String log;

    public NubomediaBuildLogs() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
