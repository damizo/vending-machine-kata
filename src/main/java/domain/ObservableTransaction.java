package domain;

import java.util.List;

public interface ObservableTransaction {

    TransactionStatus getStatus();

    List<CoinType> getInsertedCoins();

    List<CoinType> getCoinsToReturnChange();

    Integer getShelveNumber();

    void endTransaction(TransactionStatus status);

    void update(TransactionStatus insufficientMoney);
}
