package root.core.dto;

public class ErrorDTO {

    private String message;

    private Throwable exception;

    public ErrorDTO(String message, Throwable exception) {
        this.message = message;
        this.exception = exception;
    }

    public String getMessage() {
        return message;
    }

    public Throwable getException() {
        return exception;
    }
}
