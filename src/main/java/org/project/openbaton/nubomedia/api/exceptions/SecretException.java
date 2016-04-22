package org.project.openbaton.nubomedia.api.exceptions;

/**
 * Created by Carlo on 22/04/2016.
 */
public class SecretException extends Exception {

    public SecretException(){super();}

    public SecretException(Throwable throwable){ super(throwable);}

    public SecretException(String message){ super(message);}

    public SecretException(String message, Throwable throwable){ super(message,throwable); }
}
