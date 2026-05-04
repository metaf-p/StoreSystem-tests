package model.auth.common;

import java.util.UUID;

public record AuthContext(
        UUID userId,
        String token,
        String refreshToken,
        String tokenType
) {
}
