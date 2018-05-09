package domain;

import java.math.BigDecimal;

public enum CoinType {
    FIVE("5.0"), TWO("2.0"), ONE("1.0"), ZERO_FIVE("0.5"), ZERO_TWO("0.2"), ZERO_ONE("0.1");

    private final String value;

    CoinType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public BigDecimal resolveDecimalValue(){
        return new BigDecimal(getValue());
    }
}
