package hardware;

import common.Display;
import java.math.BigDecimal;
import domain.Product;

public class MachineDisplay implements Display {

    @Override
    public void displayMessage(String message){
        System.out.println(message);
    }

    public void displayMessageAboutSelectedProduct(Product product){
        displayMessage(String.format(MESSAGE_SHELVE_SELECTED, product.getName(), product.getPrice()));
    }

    public void displayMessageAboutEmptyShelve(Integer shelveNumber) {
        displayMessage(String.format(MESSAGE_EMPTY_SHELVE, shelveNumber));
    }

    public void displayMessageAboutPriceToCover(BigDecimal amountToCoverPrice) {
        displayMessage(String.format(MESSAGE_AMOUNT_TO_COVER_PRICE, amountToCoverPrice));
    }

    public void displayMessageAboutInsufficientMoney(){
        displayMessage(MESSAGE_INSUFFICIENT_MONEY);
    }

}
