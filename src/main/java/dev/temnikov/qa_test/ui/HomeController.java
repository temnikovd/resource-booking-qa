package dev.temnikov.qa_test.ui;

import dev.temnikov.qa_test.entity.User;
import dev.temnikov.qa_test.security.SecurityUser;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/ui/home")
    public String home(Authentication authentication, Model model) {

        SecurityUser principal = (SecurityUser) authentication.getPrincipal();
        User domainUser = principal.getUser();

        String fullName = domainUser.getFullName();
        String email = domainUser.getEmail();

        model.addAttribute("fullName", fullName);
        model.addAttribute("email", email);

        return "home";
    }
}
