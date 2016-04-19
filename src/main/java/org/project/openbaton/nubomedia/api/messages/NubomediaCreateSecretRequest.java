package org.project.openbaton.nubomedia.api.messages;

/**
 * Created by maa on 06.10.15.
 */
public class NubomediaCreateSecretRequest {

    private String privateKey;

    public NubomediaCreateSecretRequest() {
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
