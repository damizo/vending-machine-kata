package domain;

import lombok.Data;
import domain.Product;

@Data
public class Shelve {

    private Product product;
    private Integer productCount;

    public Shelve(Product product, Integer productCount) {
        this.product = product;
        this.productCount = productCount;
    }
}
