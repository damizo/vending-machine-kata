package common;

public interface Display {

    String MESSAGE_SHELVE_SELECTED = "Product '%s' has been chosen, price: %1.2f";
    String MESSAGE_EMPTY_SHELVE = "domain.Shelve number %d is empty. Product is not available";
    String MESSAGE_AMOUNT_TO_COVER_PRICE = "Amount to cover price of product: %1.2f";
    String MESSAGE_INSUFFICIENT_MONEY = "Insufficient money on coin holder, transaction must be canceled.";

    void displayMessage(String message);

}
