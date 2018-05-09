package exception;

import domain.CoinType;

public class CoinNotFoundException extends RuntimeException {

    public CoinNotFoundException(CoinType coinType) {
        super(String.format(ExceptionMessages.COIN_NOT_FOUND, coinType.name()));
    }
}
