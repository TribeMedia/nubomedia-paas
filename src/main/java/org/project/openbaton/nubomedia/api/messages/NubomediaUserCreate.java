package org.project.openbaton.nubomedia.api.messages;

import java.util.List;

/**
 * Created by Carlo on 19/04/2016.
 */
public class NubomediaUserCreate {

    private String username;
    private String password;
    private String mail;
    private List<String> projects;

    public NubomediaUserCreate() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }
}
