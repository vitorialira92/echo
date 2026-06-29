package liraz.echo.dto.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        int status,
        String error,
        String message,
        Instant timestamp,
        Map<String, String> fieldErrors
) {
    public static ApiError of(HttpStatus status, String message) {
        return new ApiError(status.value(), status.getReasonPhrase(), message, Instant.now(), null);
    }

    public static ApiError validation(HttpStatus status, String message, Map<String, String> fieldErrors) {
        return new ApiError(status.value(), status.getReasonPhrase(), message, Instant.now(), fieldErrors);
    }
}