import common.Observer;
import domain.Coin;
import domain.CoinType;
import domain.ObservableTransaction;
import domain.Product;
import domain.Transaction;
import domain.TransactionStatus;
import exception.ProductNotFoundException;
import hardware.CoinHolder;
import hardware.MachineDisplay;
import hardware.MachineProductPublisher;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VendingMachine implements Observer {

    private static final Logger LOGGER = LoggerFactory.getLogger(VendingMachine.class);

    private MachineDisplay machineDisplay;
    private Transaction currentTransaction;
    private List<Transaction> transactions = new ArrayList<>();
    private MachineProductPublisher productPublisher;
    private CoinHolder coinHolder;

    public VendingMachine(MachineDisplay machineDisplay, MachineProductPublisher productPublisher, CoinHolder coinHolder) {
        this.machineDisplay = machineDisplay;
        this.productPublisher = productPublisher;
        this.coinHolder = coinHolder;
    }

    public void selectShelve(Integer shelveNumber) {
        LOGGER.info("Chosen shelve: {}", shelveNumber);
        Product product = productPublisher.getProduct(shelveNumber)
            .orElseThrow(() -> new ProductNotFoundException(shelveNumber));

        if (productPublisher.shelveWithProductIsEmpty(product)) {
            machineDisplay.displayMessageAboutEmptyShelve(shelveNumber);
        } else {
            machineDisplay.displayMessageAboutSelectedProduct(product);
            currentTransaction = getOrInitTransaction(shelveNumber, product);
        }

        LOGGER.info("Current transaction: {}", currentTransaction);
    }


    public void putCoin(CoinType coin) {
        if (transactionIsNotInitialized()) {
            machineDisplay.displayMessage("You have to choose shelve");
            return;
        }

        if (isGreaterThan(currentTransaction.getProductPrice(), currentTransaction.getCoveredPrice())) {
            increaseCoveredPrice(coin);
            coinHolder.insert(coin);

            BigDecimal amountToCoverPrice = currentTransaction.getProductPrice()
                .subtract(currentTransaction.getCoveredPrice());

            if (isGreaterThan(amountToCoverPrice, BigDecimal.ZERO)) {
                machineDisplay.displayMessageAboutPriceToCover(amountToCoverPrice);
                return;
            }
        }

        BigDecimal balanceOfChange = updateChangeAndGet();
        LOGGER.info("After updated balance: {}", balanceOfChange);

        while (transactionIsProcessing()) {
            balanceOfChange = verifyAndUpdateTransaction(balanceOfChange);
            LOGGER.info("Current balance: {}", balanceOfChange);
        }

        transactions.add(currentTransaction);
    }

    public List<CoinType> returnChange(ObservableTransaction observableTransaction) {
        return observableTransaction.getCoinsToReturnChange();
    }

    public void pressCancelButton() {
        currentTransaction.setStatus(TransactionStatus.CANCELED);
        currentTransaction.setRefundedCoins(getBackCoins());
        transactions.add(currentTransaction);
    }

    public Product releaseProduct(ObservableTransaction observableTransaction) {
        Product product = productPublisher.releaseProduct(observableTransaction.getShelveNumber());
        observableTransaction.endTransaction(TransactionStatus.SUCCESS);
        return product;
    }

    private BigDecimal verifyAndUpdateTransaction(BigDecimal changeToGiveBalance) {
        BigDecimal currentBalance = changeToGiveBalance;
        for (CoinType coinType : CoinType.values()) {
            currentBalance = updateBalance(currentBalance, coinType);

            if (noAvailableCoinsToGiveAChange(currentBalance)) {
                currentTransaction.update(TransactionStatus.INSUFFICIENT_MONEY);
                machineDisplay.displayMessageAboutInsufficientMoney();
                break;
            } else if (isEqual(currentBalance, BigDecimal.ZERO)) {
                currentTransaction.update(TransactionStatus.READY_TO_RELEASE);
                break;
            }
        }
        return currentBalance;
    }

    public List<CoinType> refund(ObservableTransaction observableTransaction) {
        List<CoinType> refundedCoins = observableTransaction.getInsertedCoins()
            .stream()
            .map(coin -> coinHolder.releaseCoin(coin))
            .collect(Collectors.toList());
        currentTransaction.setRefundedCoins(refundedCoins);
        return refundedCoins;
    }

    @Override
    public void notifyAboutEndOfTransaction(ObservableTransaction observableTransaction) {
        if (productCanBeReleased(observableTransaction)) {
            releaseProduct(observableTransaction);
            returnChange(observableTransaction);
        } else if (machineHasInsufficientMoney(observableTransaction)) {
            refund(observableTransaction);
        }
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    private boolean transactionIsNotInitialized() {
        return currentTransaction == null || currentTransaction.getStatus() != TransactionStatus.IN_PROGRESS;
    }

    private BigDecimal addToReturnChangeCoinsAndUpdateBalance(BigDecimal changeToGiveBalance, CoinType coinToRelease) {
        currentTransaction.getCoinsToReturnChange().add(coinToRelease);
        return changeToGiveBalance.subtract(coinToRelease.resolveDecimalValue());
    }

    private boolean noAvailableCoinsToGiveAChange(BigDecimal changeToGive) {
        return !isEqual(changeToGive, BigDecimal.ZERO)
            && changeToGiveIsSmallerThanAllAvailableCoins(changeToGive);
    }

    private BigDecimal updateChangeAndGet() {
        currentTransaction.setChange(currentTransaction.getCoveredPrice().subtract(currentTransaction.getProductPrice()));
        return currentTransaction.getChange();
    }

    private boolean transactionIsProcessing() {
        return currentTransaction.getStatus() == TransactionStatus.IN_PROGRESS;
    }

    private BigDecimal updateBalance(BigDecimal changeToGiveBalance, CoinType coinType) {
        BigDecimal balance = changeToGiveBalance;
        if (isGreaterThanOrEqual(balance, coinType.resolveDecimalValue())
            && coinHolder.containsCoinToChange(coinType)) {
            CoinType coinToRelease = coinHolder.releaseCoin(coinType);
            balance = addToReturnChangeCoinsAndUpdateBalance(balance, coinToRelease);
        }
        return balance;
    }

    private boolean machineHasInsufficientMoney(ObservableTransaction observableTransaction) {
        return observableTransaction.getStatus() == TransactionStatus.INSUFFICIENT_MONEY;
    }

    private boolean changeToGiveIsSmallerThanAllAvailableCoins(BigDecimal changeToGive) {
        List<Coin> availableCoins = coinHolder.getAvailableCoins();
        return availableCoins.stream()
            .filter(coin -> isSmallerThan(changeToGive, coin.getType().resolveDecimalValue()))
            .count() == availableCoins.size();
    }

    private void increaseCoveredPrice(CoinType coin) {
        currentTransaction.getInsertedCoins().add(coin);
        BigDecimal currentCoveredPrice = currentTransaction.getCoveredPrice();
        currentTransaction.setCoveredPrice(currentCoveredPrice.add(coin.resolveDecimalValue()));
    }

    private boolean productCanBeReleased(ObservableTransaction observable) {
        return observable.getStatus() == TransactionStatus.READY_TO_RELEASE;
    }


    private List<CoinType> getBackCoins() {
        return currentTransaction.getInsertedCoins()
            .stream()
            .map(coin -> coinHolder.releaseCoin(coin))
            .collect(Collectors.toList());
    }

    private Transaction getOrInitTransaction(Integer shelveNumber, Product product) {
        return transactionIsNotInitialized() ?
            new Transaction(shelveNumber, product, this) : currentTransaction;
    }

    private boolean isGreaterThan(BigDecimal firstValue, BigDecimal secondValue) {
        return firstValue.compareTo(secondValue) > 0;
    }

    private boolean isSmallerThan(BigDecimal firstValue, BigDecimal secondValue) {
        return firstValue.compareTo(secondValue) < 0;
    }

    private boolean isGreaterThanOrEqual(BigDecimal firstValue, BigDecimal secondValue) {
        return firstValue.compareTo(secondValue) > 0 || firstValue.compareTo(secondValue) == 0;
    }

    private boolean isEqual(BigDecimal firstValue, BigDecimal secondValue) {
        return firstValue.compareTo(secondValue) == 0;
    }
}


