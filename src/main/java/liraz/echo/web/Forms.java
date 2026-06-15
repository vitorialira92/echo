package liraz.echo.web;

public final class Forms {

    private Forms() {
    }

    public static String nullIfBlank(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
