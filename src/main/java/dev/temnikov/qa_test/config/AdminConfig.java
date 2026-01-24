package dev.temnikov.qa_test.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AdminConfig {

    private final String adminCreationSecret;

    public AdminConfig(@Value("${qa-test.admin-creation-secret}") String adminCreationSecret) {
        this.adminCreationSecret = adminCreationSecret;
    }

    public String getAdminCreationSecret() {
        return adminCreationSecret;
    }
}