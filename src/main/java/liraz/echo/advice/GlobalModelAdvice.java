package liraz.echo.advice;

import jakarta.servlet.http.HttpServletRequest;
import liraz.echo.domain.music.Country;
import liraz.echo.domain.music.Genre;
import liraz.echo.domain.rating.Feeling;
import liraz.echo.web.Format;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAdvice {
    private final Format format;

    public GlobalModelAdvice(Format format) {
        this.format = format;
    }

    @ModelAttribute("fmt")
    public Format fmt() {
        return format;
    }

    @ModelAttribute("countries")
    public Country[] countries() {
        return Country.values();
    }

    @ModelAttribute("genres")
    public Genre[] genres() {
        return Genre.values();
    }

    @ModelAttribute("feelings")
    public Feeling[] feelings() {
        return Feeling.values();
    }

    @ModelAttribute("currentUri")
    public String currentUri(HttpServletRequest request) {
        return request.getRequestURI();
    }
}
