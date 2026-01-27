package dev.temnikov.qa_test.ui.model;

import lombok.Data;

@Data
public class RegistrationForm {

    private String email;
    private String fullName;
    private String password;
    private String confirmPassword;
}