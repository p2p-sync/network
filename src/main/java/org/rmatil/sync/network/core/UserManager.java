package org.rmatil.sync.network.core;

import org.rmatil.sync.network.api.IClientManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.api.IUserManager;
import org.rmatil.sync.network.core.model.ClientLocation;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;

/**
 * Manages user's data in the DHT one he logs in
 */
public class UserManager implements IUserManager {

    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

    protected IClientManager clientManager;
    protected ClientLocation clientLocation;

    public UserManager(IClientManager clientManager, ClientLocation clientLocation) {
        this.clientManager = clientManager;
        this.clientLocation = clientLocation;
    }

    @Override
    public boolean isRegistered(String username)
            throws InputOutputException {

        PublicKey publicKey = this.clientManager.getPublicKey(username);

        // if no public key is found for an user, then we assume
        // the username is not used yet
        return null != publicKey;
    }

    @Override
    public boolean login(IUser user) {
        try {
            this.clientManager.addClientLocation(user, this.clientLocation);
            this.clientManager.addPrivateKey(user);
            this.clientManager.addPublicKey(user);
            this.clientManager.addSalt(user);
        } catch (InputOutputException e) {
            logger.error("Failed to add client location during login. Message: " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean logout(IUser user) {
        try {
            this.clientManager.removeClientLocation(user, this.clientLocation);
        } catch (InputOutputException e) {
            logger.error("Failed to remove client location during logout. Message: " + e.getMessage());
            return false;
        }

        return true;
    }

}
