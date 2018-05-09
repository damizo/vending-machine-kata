package exception;

import exception.ExceptionMessages;

public class ShelveNotExists extends RuntimeException {

    public ShelveNotExists(Integer shelveNumber) {
            super(String.format(ExceptionMessages.SHELVE_NOT_EXISTS, shelveNumber));
    }
}
