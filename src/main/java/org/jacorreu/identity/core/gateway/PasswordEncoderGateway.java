package org.jacorreu.identity.core.gateway;

public interface PasswordEncoderGateway {
    boolean matches(String rawPassword, String encodedPassword);
    String encode(String rawPassword);
}
