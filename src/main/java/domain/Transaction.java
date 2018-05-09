package domain;

import common.Observer;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Data;

@Data
public class Transaction implements ObservableTransaction {

    private TransactionStatus status;
    private Integer shelveNumber;
    private BigDecimal productPrice;
    private BigDecimal coveredPrice;
    private BigDecimal change;
    private List<CoinType> coinsToReturnChange = new ArrayList<>();
    private List<CoinType> insertedCoins = new ArrayList<>();
    private List<CoinType> refundedCoins = new ArrayList<>();
    private List<Observer> observers = new ArrayList<>();


    public Transaction(Integer shelveNumber, Product product, Observer... observers) {
        this.productPrice = product.getPrice();
        this.shelveNumber = shelveNumber;
        this.status = TransactionStatus.IN_PROGRESS;
        this.coveredPrice = BigDecimal.ZERO;
        this.change = BigDecimal.ZERO;
        this.observers = Arrays.asList(observers);
    }

    @Override
    public void endTransaction(TransactionStatus status) {
        this.status = status;
    }

    @Override
    public void update(TransactionStatus status) {
        this.status = status;
        for (Observer observer : observers) {
            observer.notifyAboutEndOfTransaction(this);
        }
    }
}
