package data.auth;

import model.auth.request.EditUserRequest;
import model.auth.request.RegisterUserRequest;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

public final class AuthTestData {

    private AuthTestData() {
    }

    public static RegisterUserRequest uniqueUser() {
        return newUserWithEmail(randomEmailGenerator());
    }

    public static RegisterUserRequest newUserWithEmail(String email) {
        return new RegisterUserRequest(
                "TestUser",
                email,
                "StrongPass123!"
        );
    }

    public static EditUserRequest editUserRequest() {
        return new EditUserRequest(
                randomEmailGenerator(),
                randomNameGenerator()
        );
    }

    public static EditUserRequest editUserRequestWithEmail(String email) {
        return new EditUserRequest(email, null);
    }

    public static EditUserRequest editUserRequestWithEmailOnly() {
        return new EditUserRequest(
                randomEmailGenerator(),
                null
        );
    }

    public static EditUserRequest editUserRequestWithNameOnly() {
        return new EditUserRequest(
                null,
                randomNameGenerator()
        );
    }

    private static String randomEmailGenerator() {
        return "test-user-" + UUID.randomUUID() + "@test.com";
    }

    private static String randomNameGenerator() {
        return RandomStringUtils.secure().nextAlphabetic(3, 51);
    }
}
