package org.jacorreu;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;

@SpringBootApplication(exclude = { UserDetailsServiceAutoConfiguration.class })
public class JaCorreuApplication {

    public static void main(String[] args) {
        SpringApplication.run(JaCorreuApplication.class, args);
    }

}
