package edu.robertob.ayd2_p1_backend.core.enums;

import edu.robertob.ayd2_p1_backend.core.exceptions.InvalidTokenException;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum JwtErrorEnum {

        JWT_INVALID(new InvalidTokenException(
                        ErrorCodeMessageEnum.JWT_INVALID.getCode())),

        JWT_NO_EXPIRATION(
                        new InvalidTokenException(
                                        ErrorCodeMessageEnum.JWT_NO_EXPIRATION.getCode())),
        JWT_NO_CLIENT_ID(
                        new InvalidTokenException(
                                        ErrorCodeMessageEnum.JWT_NO_CLIENT_ID.getCode())),

        JWT_NO_AUTHORITIES(
                        new InvalidTokenException(
                                        ErrorCodeMessageEnum.JWT_NO_AUTHORITIES.getCode())),

        JWT_NO_USER_TYPE(
                        new InvalidTokenException(
                                        ErrorCodeMessageEnum.JWT_NO_USER_TYPE.getCode())),

        JWT_NO_USERNAME(
                        new InvalidTokenException(
                                        ErrorCodeMessageEnum.JWT_NO_USERNAME.getCode())),

        CLAIM_TYPE_MISMATCH(
                        new InvalidTokenException(
                                        ErrorCodeMessageEnum.CLAIM_TYPE_MISMATCH.getCode())),

        JWT_UNSUPPORTED(new InvalidTokenException(
                        ErrorCodeMessageEnum.JWT_UNSUPPORTED.getMessage())),
        JWT_MALFORMED(new InvalidTokenException(
                        ErrorCodeMessageEnum.JWT_MALFORMED.getMessage())),
        JWT_SIGNATURE_INVALID(new InvalidTokenException(
                        ErrorCodeMessageEnum.JWT_SIGNATURE_INVALID.getMessage())),
        JWT_EXPIRED(new InvalidTokenException(
                        ErrorCodeMessageEnum.JWT_EXPIRED.getMessage())),
        JWT_ILLEGAL_ARGUMENT(new InvalidTokenException(
                        ErrorCodeMessageEnum.JWT_ILLEGAL_ARGUMENT.getMessage()));

        private final InvalidTokenException invalidTokenException;

}
