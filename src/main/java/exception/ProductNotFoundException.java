package exception;


public class ProductNotFoundException extends RuntimeException {


    public ProductNotFoundException(String name) {
        super(String.format(ExceptionMessages.PRODUCT_NOT_FOUND, name));
    }

    public ProductNotFoundException(Integer shelveNumber) {
        super(String.format(ExceptionMessages.EMPTY_SHELVE, shelveNumber));
    }
}
