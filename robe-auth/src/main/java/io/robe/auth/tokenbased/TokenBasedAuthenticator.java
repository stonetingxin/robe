package io.robe.auth.tokenbased;

import com.google.common.base.Optional;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.robe.auth.data.entry.PermissionEntry;
import io.robe.auth.data.entry.RoleEntry;
import io.robe.auth.data.entry.ServiceEntry;
import io.robe.auth.data.entry.UserEntry;
import io.robe.auth.data.store.ServiceStore;
import io.robe.auth.data.store.UserStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Authenticator implementation for token based authentication.
 */
public class TokenBasedAuthenticator implements Authenticator<String, Token> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TokenBasedAuthenticator.class);

    private final ServiceStore serviceStore;
    private final UserStore userStore;

    /**
     * Creates an instance of TokenBasedAuthenticator with the store classes.
     *
     * @param userStore    Store for getting user.
     * @param serviceStore Store for getting service info.
     */
    public TokenBasedAuthenticator(UserStore userStore, ServiceStore serviceStore) {
        this.userStore = userStore;
        this.serviceStore = serviceStore;
    }

    /**
     * Creates {@link com.google.common.base.Optional} {@link io.robe.auth.Credentials} instance from provided tokenString
     *
     * @param tokenString tokenString to decode.
     * @return Optional instance of a {@link io.robe.auth.Credentials} which created from tokenString
     * @throws AuthenticationException
     */
    @Override
    public Optional<Token> authenticate(String tokenString) throws AuthenticationException {
        LOGGER.debug("Authenticating from database:  " + tokenString);
        try {
            // Decode tokenString and get user
            Token token = TokenFactory.getInstance().createToken(tokenString);

            Optional<UserEntry> user = (Optional<UserEntry>) userStore.findByUsername(token.getUsername());
            if (!user.isPresent()) {
                LOGGER.warn("User is not available: " + tokenString);
                return Optional.absent();
            }
            // If user exists and active than check Service Permissions for authorization controls
            if (user.get().isActive()) {

                if (token.getPermissions() == null) {
                    LOGGER.debug("Loading Permissions from DB: " + tokenString);
                    Set<String> permissions = new HashSet<String>();
                    Set<PermissionEntry> rolePermissions = new HashSet<PermissionEntry>();

                    //If user role is a group than add sub role permissions to group
                    getAllRolePermissions(user.get().getRole(), rolePermissions);

                    for (PermissionEntry permission : rolePermissions) {
                        if (permission.getType().equals(PermissionEntry.Type.SERVICE)) {
                            Optional<? extends ServiceEntry> service = serviceStore.findByCode(permission.getRestrictedItemId());
                            if (service.isPresent()) {
                                permissions.add(service.get().getPath() + ":" + service.get().getMethod());
                            }
                        }
                    }
                    // Create credentials with user info and permission list
                    token.setPermissions(Collections.unmodifiableSet(permissions));
                } else {
                    LOGGER.debug("Loading Permissions from Cache: " + tokenString);
                }

                return Optional.fromNullable(token);
            }
        } catch (Exception e) {
            LOGGER.error(tokenString, e);
        }
        return Optional.absent();

    }


    /**
     * Fill  permission list  with role and sub-role permissions recursively.
     *
     * @param parent          Role to traverse.
     * @param rolePermissions list of all permissions of the given role.
     */
    private void getAllRolePermissions(RoleEntry parent, Set<PermissionEntry> rolePermissions) {
        rolePermissions.addAll(parent.getPermissions());
        for (RoleEntry role : parent.getRoles()) {
            getAllRolePermissions(role, rolePermissions);
        }
    }


}


