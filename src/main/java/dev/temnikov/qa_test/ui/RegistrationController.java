package dev.temnikov.qa_test.ui;

import dev.temnikov.qa_test.api.dto.RequestUserDto;
import dev.temnikov.qa_test.entity.UserRole;
import dev.temnikov.qa_test.service.UserService;
import dev.temnikov.qa_test.ui.model.RegistrationForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @GetMapping("/register")
    public String registerForm(Authentication authentication, Model model) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/ui/home";
        }
        if (!model.containsAttribute("form")) {
            model.addAttribute("form", new RegistrationForm());
        }
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @ModelAttribute("form") RegistrationForm form,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/ui/home";
        }

        if (form.getEmail() == null || form.getEmail().isBlank()) {
            bindingResult.reject("email", "Email is required");
        }
        if (form.getPassword() == null || form.getPassword().isBlank()) {
            bindingResult.reject("password", "Password is required");
        }
        if (form.getConfirmPassword() == null || !form.getConfirmPassword().equals(form.getPassword())) {
            bindingResult.reject("confirmPassword", "Passwords do not match");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("form", form);
            model.addAttribute("error", "Please fix the errors in the form");
            return "register";
        }

        RequestUserDto dto = new RequestUserDto(
                form.getEmail(),
                form.getFullName(),
                UserRole.USER,
                form.getPassword()
        );

        userService.create(dto, null);

        return "redirect:/login?registered";
    }
}
