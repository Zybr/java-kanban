package models.managers.filebacked.exceptions;

public class ManagerLoadException extends RuntimeException {
    public ManagerLoadException(String message) {
        super(message);
    }

    public ManagerLoadException() {
        super("Failed to load tasks");
    }
}
