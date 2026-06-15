package liraz.echo.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserForm {

    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(min = 4, max = 100)
    private String password;

    @NotBlank
    @Size(max = 120)
    private String name;
}
