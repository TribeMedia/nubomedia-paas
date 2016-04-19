package org.project.openbaton.nubomedia.api.openshift.json;

import java.util.List;

/**
 * Created by Carlo on 19/04/2016.
 */
public class ProjectList {

    private final static String kind = "ProjectList";
    private final static String apiVersion = "v1";
    private Metadata metadata;
    private List<Project> items;

    public ProjectList() {
    }

    public ProjectList(Metadata metadata, List<Project> items) {
        this.metadata = metadata;
        this.items = items;
    }

    public static String getKind() {
        return kind;
    }

    public static String getApiVersion() {
        return apiVersion;
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    public List<Project> getItems() {
        return items;
    }

    public void setItems(List<Project> items) {
        this.items = items;
    }
}
