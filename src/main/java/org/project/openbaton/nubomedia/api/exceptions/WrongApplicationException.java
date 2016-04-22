package org.project.openbaton.nubomedia.api.exceptions;

/**
 * Created by Carlo on 21/04/2016.
 */
public class WrongApplicationException extends Exception {

    public WrongApplicationException(){super();}

    public WrongApplicationException(Throwable throwable){ super(throwable);}

    public WrongApplicationException(String message){ super(message);}

    public WrongApplicationException(String message, Throwable throwable){ super(message,throwable); }
}
