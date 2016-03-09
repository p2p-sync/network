package org.rmatil.sync.network.api;

import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.persistence.exceptions.InputOutputException;

/**
 * A manager abstracting the tasks
 * to log users in or out and to check
 * whether an user is already registered
 */
public interface IUserManager {

    /**
     * Checks whether the user with
     * the given user name is already registered
     *
     * @param username The username to check
     *
     * @return True, if not registered yet, false otherwise
     *
     * @throws InputOutputException If connecting to the storage layer fails
     */
    boolean isRegistered(String username)
            throws InputOutputException;

    /**
     * Logs the given user in and adds the specified
     * node location to the list of connected clients.
     * Furthermore, the private and the public key as well as the
     * salt are added to the storage layer.
     *
     * @param user         The user to login
     * @param nodeLocation The node location of the user
     *
     * @return True, if login was successful, false otherwise
     */
    boolean login(IUser user, NodeLocation nodeLocation);

    /**
     * Logs the given user out and removes the specified
     * node location from the list of connected nodes.
     *
     * @param user         The user to logout
     * @param nodeLocation The node location to remove
     *
     * @return True, if logout was successful, false otherwise
     */
    boolean logout(IUser user, NodeLocation nodeLocation);

}