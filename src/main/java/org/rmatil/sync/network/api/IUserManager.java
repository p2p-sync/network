package org.rmatil.sync.network.api;

import org.rmatil.sync.persistence.exceptions.InputOutputException;

public interface IUserManager {

    boolean isRegistered(String username)
            throws InputOutputException;

    boolean login(IUser user);

    boolean logout(IUser user);

}