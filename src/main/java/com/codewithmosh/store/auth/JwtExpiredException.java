package com.codewithmosh.store.auth;

public class JwtExpiredException extends RuntimeException {
    public JwtExpiredException(String refreshTokenIsExpired) {
        super(refreshTokenIsExpired);
    }
}
