package exception;

import exception.ExceptionMessages;

public class EmptyShelveException extends RuntimeException {
    public EmptyShelveException(Integer shelveNumber) {
        super(String.format(ExceptionMessages.EMPTY_SHELVE, shelveNumber));
    }
}
