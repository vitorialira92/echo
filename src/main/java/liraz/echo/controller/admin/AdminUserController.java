package liraz.echo.controller.admin;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("users", userService.findAll());
        return "admin/users";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        if (!model.containsAttribute("userForm")) {
            model.addAttribute("userForm", new UserForm());
        }
        return "admin/user-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute("userForm") UserForm form,
                         BindingResult result,
                         RedirectAttributes redirectAttributes) {
        if (form.getUsername() != null && userService.usernameTaken(form.getUsername())) {
            result.rejectValue("username", "user.username.taken");
        }
        if (result.hasErrors()) {
            return "admin/user-form";
        }
        userService.register(form.getUsername(), form.getPassword(), form.getName(), Role.ADMIN);
        redirectAttributes.addFlashAttribute("flashSuccess", "admin.created");
        return "redirect:/admin/users";
    }
}
