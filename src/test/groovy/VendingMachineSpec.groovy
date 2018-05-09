import hardware.CoinHolder
import domain.CoinType
import domain.Product
import domain.Transaction
import domain.TransactionStatus
import hardware.MachineDisplay
import hardware.MachineProductPublisher
import spock.lang.Shared
import spock.lang.Specification

class VendingMachineSpec extends Specification {

    private VendingMachine vendingMachine

    private MachineDisplay display

    private MachineProductPublisher productPublisher

    private CoinHolder coinHolder

    @Shared
    private Integer randomShelveNumber = 1

    @Shared
    private List<Product> randomProductsToAdd = Arrays.asList(
            new Product("Fanta 0.5l", 3.50),
            new Product("Chocolate", 5.50)
    )

    @Shared
    private Integer firstIndex = 0

    @Shared
    private Integer secondIndex = 1

    void setup() {
        display = Spy(MachineDisplay.class)
        productPublisher = Spy(MachineProductPublisher.class)
        coinHolder = Spy(CoinHolder.class)
        vendingMachine = Spy(VendingMachine.class,
                constructorArgs: [display, productPublisher, coinHolder])
    }

    void cleanup() {
        coinHolder.clear()
        productPublisher.clear()
    }

    def "should not throw a product when shelve is empty"() {
        setup:
        Product fanta = randomProductsToAdd.get(firstIndex)
        productPublisher.addProduct(randomShelveNumber, fanta)

        when: "number of product's shelve is selected"
        vendingMachine.selectShelve(randomShelveNumber)

        then: "price of product is displayed on a screen"
        1 * display.displayMessageAboutEmptyShelve(randomShelveNumber)
    }

    def "should show price to cover a product after insert a coin"() {
        setup:
        Product chocolate = randomProductsToAdd.get(secondIndex)
        productPublisher.addProduct(randomShelveNumber, 10, chocolate)

        BigDecimal expectedAmountToCoverAfterFirstInsert = new BigDecimal("4.50")
        BigDecimal expectedAmountToCoverAfterSecondInsert = new BigDecimal("4.30")

        when: "number of product's shelve is selected"
        vendingMachine.selectShelve(randomShelveNumber)

        and: "put coin into machine"
        vendingMachine.putCoin(CoinType.ONE)
        vendingMachine.putCoin(CoinType.ZERO_TWO)

        then:
        1 * display.displayMessageAboutPriceToCover(expectedAmountToCoverAfterFirstInsert)
        1 * display.displayMessageAboutPriceToCover(expectedAmountToCoverAfterSecondInsert)
    }

    def "should get money back when user inserted insufficient money"() {
        setup:
        Product chocolate = randomProductsToAdd.get(secondIndex)
        productPublisher.addProduct(randomShelveNumber, 10, chocolate)
        BigDecimal expectedAmountToCoverPrice = new BigDecimal("4.20")

        when: "number of product's shelve is selected"
        vendingMachine.selectShelve(randomShelveNumber)

        then: "price of product is displayed on a screen"
        1 * display.displayMessageAboutSelectedProduct(chocolate)

        when: "put coins into vending machine"
        vendingMachine.putCoin(CoinType.ZERO_ONE)
        vendingMachine.putCoin(CoinType.ZERO_TWO)
        vendingMachine.putCoin(CoinType.ZERO_FIVE)
        vendingMachine.putCoin(CoinType.ZERO_FIVE)

        then:
        1 * display.displayMessageAboutPriceToCover(expectedAmountToCoverPrice)

        and: "cancel button is pressed and machine return inserted coins"
        vendingMachine.pressCancelButton()

        Transaction transaction = getLastTransaction()

        transaction.getStatus() == TransactionStatus.CANCELED
        transaction.getRefundedCoins().containsAll(CoinType.ZERO_ONE,
                CoinType.ZERO_TWO,
                CoinType.ZERO_FIVE,
                CoinType.ZERO_FIVE)
    }

    def "should get money back when vending machine does not have enough money to change"() {
        setup:
        Product chocolate = randomProductsToAdd.get(secondIndex)
        productPublisher.addProduct(randomShelveNumber, 10, chocolate)

        coinHolder.insertCoinsToChange(CoinType.ZERO_ONE, CoinType.ZERO_TWO)

        when: "number of product's shelve is selected"
        vendingMachine.selectShelve(randomShelveNumber)

        and: "put coins into vending machine"
        vendingMachine.putCoin(CoinType.FIVE)
        vendingMachine.putCoin(CoinType.ONE)

        then: "machine returns coins"
        1 * vendingMachine.refund(_)

        Transaction transaction = getLastTransaction()
        transaction.getStatus() == TransactionStatus.INSUFFICIENT_MONEY
        transaction.getRefundedCoins().containsAll(CoinType.FIVE, CoinType.ONE)

    }

    def "should return change after purchase of product"() {
        setup:
        Product fanta = randomProductsToAdd.get(firstIndex)
        productPublisher.addProduct(randomShelveNumber, 10, fanta)

        BigDecimal expectedAmountToCoverPrice = new BigDecimal("1.50")

        List<CoinType> expectedChange = Arrays.asList(CoinType.ZERO_TWO,
                CoinType.ZERO_TWO,
                CoinType.ZERO_ONE)

        coinHolder.insertCoinsToChange(CoinType.ONE,
                CoinType.ZERO_TWO,
                CoinType.ZERO_TWO,
                CoinType.ZERO_ONE)

        when: "number of product's shelve is selected"
        vendingMachine.selectShelve(randomShelveNumber)

        then: "price of product is displayed on a screen"
        1 * display.displayMessageAboutSelectedProduct(fanta)

        when: "put coin into vending machine"
        vendingMachine.putCoin(CoinType.TWO)

        then:
        1 * display.displayMessageAboutPriceToCover(expectedAmountToCoverPrice)

        when: "put another coin into vending machine"
        vendingMachine.putCoin(CoinType.TWO)

        then: "machine returns product"
        1 * vendingMachine.releaseProduct(_)
        1 * productPublisher.releaseProduct(randomShelveNumber)

        and: "machine returns change"
        1 * vendingMachine.returnChange(_)

        Transaction transaction = getLastTransaction()

        transaction.getStatus() == TransactionStatus.SUCCESS
        expectedChange.containsAll(transaction.getCoinsToReturnChange())

    }

    private Transaction getLastTransaction() {
        return vendingMachine.getTransactions().get(firstIndex)
    }


}
