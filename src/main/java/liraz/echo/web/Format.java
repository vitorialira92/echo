package liraz.echo.web;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component("fmt")
public class Format {
    private static final DateTimeFormatter DATE_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String dateTime(LocalDateTime value) {
        return value == null ? "" : value.format(DATE_TIME);
    }

    public String duration(Integer seconds) {
        if (seconds == null) {
            return "";
        }
        int minutes = seconds / 60;
        int remainder = seconds % 60;
        return minutes + ":" + (remainder < 10 ? "0" : "") + remainder;
    }
}
