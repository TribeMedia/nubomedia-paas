package org.project.openbaton.nubomedia.api.persistence;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

/**
 * Created by Carlo on 19/04/2016.
 */
@Entity
public class User {

    @Id
    private String id;
    @Column (unique = true)
    private String username;
    private String password;
    private String mail;
    @ElementCollection (fetch = FetchType.EAGER)
    private List<String> projects;
    private boolean admin;

    public User() {
    }

    public User(String username, String password, String mail, List<String> projects, boolean admin) {
        this.username = username;
        this.password = password;
        this.mail = mail;
        this.projects = projects;
        this.admin = admin;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public List<String> getProjects() {
        return projects;
    }

    public void setProjects(List<String> projects) {
        this.projects = projects;
    }

    @PrePersist
    private void ensureId(){
        this.id = UUID.randomUUID().toString();
    }
}
