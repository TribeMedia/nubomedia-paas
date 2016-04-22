package org.project.openbaton.nubomedia.api.messages;

/**
 * Created by Carlo on 22/04/2016.
 */
public class NubomediaProjectCreate {

    private String name;
    private String description;

    public NubomediaProjectCreate() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
