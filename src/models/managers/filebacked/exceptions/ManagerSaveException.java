package models.managers.filebacked.exceptions;

public class ManagerSaveException extends RuntimeException {
    public ManagerSaveException(String message) {
        super(message);
    }

    public ManagerSaveException() {
        super("Failed to save tasks");
    }
}
