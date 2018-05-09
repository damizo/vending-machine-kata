package hardware;

import domain.Product;
import domain.Shelve;
import exception.EmptyShelveException;
import exception.ProductNotFoundException;
import exception.ShelveNotExists;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class MachineProductPublisher {

    private Map<Integer, Shelve> shelvesWithProducts = new HashMap() {{
        put(15, new Shelve(new Product("Snickers", new BigDecimal("1.20")), 5));
        put(16, new Shelve(new Product("Mars", new BigDecimal("1.20")), 5));
        put(17, new Shelve(new Product("Oshee", new BigDecimal("3.20")), 5));
        put(18, new Shelve(new Product("Cola", new BigDecimal("3.00")), 5));
    }};


    public Shelve getShelve(Integer shelveNumber) {
        return Optional
            .ofNullable(shelvesWithProducts
                .get(shelveNumber))
            .orElseThrow(() -> new ShelveNotExists(shelveNumber));
    }

    public Optional<Product> getProduct(Integer shelveNumber) {
        Shelve shelve = getShelve(shelveNumber);

        if (shelve == null) {
            throw new EmptyShelveException(shelveNumber);
        }

        return Optional.of(shelve.getProduct());

    }

    public boolean shelveWithProductIsEmpty(Product product) {
        return shelvesWithProducts.entrySet()
            .stream()
            .filter(entry -> entry.getValue().getProduct().getName().equalsIgnoreCase(product.getName()))
            .findAny()
            .orElseThrow(() -> new ProductNotFoundException(product.getName()))
            .getValue()
            .getProductCount() <= 0;

    }

    public void addProduct(Integer shelveNumber, Product product) {
        shelvesWithProducts.put(shelveNumber, new Shelve(product, 0));
    }

    public void addProduct(Integer shelveNumber, Integer countOfProducts, Product product) {
        shelvesWithProducts.put(shelveNumber, new Shelve(product, countOfProducts));
    }

    public Product releaseProduct(Integer shelveNumber) {
        Shelve shelve = getShelve(shelveNumber);
        shelve.setProductCount(shelve.getProductCount() - 1);
        return shelve.getProduct();
    }

    public void clear() {
        shelvesWithProducts.clear();
    }
}
