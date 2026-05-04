package data.auth;

import java.util.Set;
import java.util.UUID;

public final class UserCleanup {
    private final Set<UUID> users;

    public UserCleanup(Set<UUID> users) {
        this.users = users;
    }

    public void addUser(UUID userId) {
        users.add(userId);
    }
}
