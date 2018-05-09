package hardware;

import domain.Coin;
import domain.CoinType;
import exception.CoinNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

public class CoinHolder {

    private static final Map<CoinType, Coin> coins = new HashMap<>();

    public CoinType releaseCoin(CoinType coinType) {
        Coin coin = Optional
            .of(coins.get(coinType))
            .orElseThrow(() -> new CoinNotFoundException(coinType));

        coin = updateCountOfCoins(coin, coin.getCount() - 1);
        coins.put(coinType, coin);
        return coin.getType();
    }

    public void insert(CoinType coinType) {
        Coin coin = Optional.ofNullable(coins.get(coinType))
            .orElse(new Coin(0, coinType));

        coin = updateCountOfCoins(coin, coin.getCount() + 1);

        coins.put(coinType, coin);
    }

    private Coin updateCountOfCoins(Coin coin, int i) {
        Integer coinCount = i;
        coin.setCount(coinCount);
        return coin;
    }


    public List<Coin> getAvailableCoins() {
        return coins.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getCount() > 0)
            .map(Entry::getValue)
            .collect(Collectors.toList());
    }

    public boolean containsCoinToChange(CoinType coinType) {
        Coin coin = coins.get(coinType);
        return getAvailableCoins().contains(coin);
    }

    public void insertCoinsToChange(CoinType... coinType) {
        for (CoinType coin : coinType){
            insert(coin);
        }
    }

    public void clear() {
        coins.clear();
    }
}
