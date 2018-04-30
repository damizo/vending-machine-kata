import spock.lang.Specification

class VendingMachineSpec extends Specification {

    def setup() {
        "Running tests..."
    }

    def "should not throw a product when shelve is empty"() {
        when: "number of product is chosen"

        then: "price of product is displayed on a screen"

        when: "put coins into vending machine"

        then: "machine display warning about empty shelve"
    }

    def "should not return product when chosen coin is not one of the accepted denominations"() {
        when: "number of product is chosen"

        then: "price of product is displayed on a screen"

        when: "put coins into vending machine"

        then: "machine display warning and returns coin"

    }

    def "should get money back when user inserted insufficient money"() {
        when: "number of product is chosen"

        then: "price of product is displayed on a screen"

        when: "put coins into vending machine"

        and: "cancel button is pressed"

        then: "machine returns inserted coins"
    }

    def "should get money back when vending machine does not have enough money to change"() {
        when: "number of product is chosen"

        and: "put coins into vending machine"

        then: "machine returns coins because it does not contain coins to change"

    }

    def "should return change after purchase of cola "() {
        when: "number of product is chosen"

        and: "put coins into vending machine"

        then: "machine returns product"

        and: "machine returns change"

    }


}
