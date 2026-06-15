package liraz.echo.controller.auth;

import jakarta.validation.Valid;
import liraz.echo.domain.user.Role;
import liraz.echo.service.UserService;
import liraz.echo.web.form.UserForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserForm());
        }
        return "signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("userForm") UserForm form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (form.getUsername() != null && userService.usernameTaken(form.getUsername())) {
            result.rejectValue("username", "user.username.taken");
        }
        if (result.hasErrors()) {
            return "signup";
        }
        userService.register(form.getUsername(), form.getPassword(), form.getName(), Role.USER);
        redirectAttributes.addFlashAttribute("flashSuccess", "signup.success");
        return "redirect:/login";
    }
}
