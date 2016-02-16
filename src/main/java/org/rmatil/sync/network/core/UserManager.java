package org.rmatil.sync.network.core;

import org.rmatil.sync.network.api.INodeManager;
import org.rmatil.sync.network.api.IUser;
import org.rmatil.sync.network.api.IUserManager;
import org.rmatil.sync.network.core.model.NodeLocation;
import org.rmatil.sync.persistence.exceptions.InputOutputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PublicKey;

/**
 * Manages user's data in the DHT one he logs in
 */
public class UserManager implements IUserManager {

    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

    protected INodeManager nodeManager;
    protected NodeLocation nodeLocation;

    public UserManager(INodeManager nodeManager, NodeLocation nodeLocation) {
        this.nodeManager = nodeManager;
        this.nodeLocation = nodeLocation;
    }

    @Override
    public boolean isRegistered(String username)
            throws InputOutputException {

        PublicKey publicKey = this.nodeManager.getPublicKey(username);

        // if no public key is found for an user, then we assume
        // the username is not used yet
        return null != publicKey;
    }

    @Override
    public boolean login(IUser user) {
        try {
            this.nodeManager.addNodeLocation(user, this.nodeLocation);
            this.nodeManager.addPrivateKey(user);
            this.nodeManager.addPublicKey(user);
            this.nodeManager.addSalt(user);
        } catch (InputOutputException e) {
            logger.error("Failed to add node location during login. Message: " + e.getMessage());
            return false;
        }

        return true;
    }

    @Override
    public boolean logout(IUser user) {
        try {
            this.nodeManager.removeNodeLocation(user, this.nodeLocation);
        } catch (InputOutputException e) {
            logger.error("Failed to remove node location during logout. Message: " + e.getMessage());
            return false;
        }

        return true;
    }

}
