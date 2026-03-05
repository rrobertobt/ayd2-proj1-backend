package edu.robertob.ayd2_p1_backend.auth.users.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RolesEnum {

    SYSTEM_ADMIN("SYSTEM_ADMIN"),
    PROJECT_ADMIN("PROJECT_ADMIN"),
    DEVELOPER("DEVELOPER"),
    ;

    private final String code;
}
