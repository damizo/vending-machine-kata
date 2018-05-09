package exception;

import exception.ExceptionMessages;

public class ShelveNotExistsException extends RuntimeException {

    public ShelveNotExistsException(Integer shelveNumber) {
            super(String.format(ExceptionMessages.SHELVE_NOT_EXISTS, shelveNumber));
    }
}
