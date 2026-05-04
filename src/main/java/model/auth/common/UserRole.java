package model.auth.common;

import com.fasterxml.jackson.annotation.JsonValue;

public enum UserRole {
    CUSTOMER("customer"),
    OPERATOR("operator"),
    ADMIN("admin"),
    ;

    private final String value;

    UserRole(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return value;
    }
}
