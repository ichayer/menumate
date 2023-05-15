package ar.edu.itba.paw.persistance;

import ar.edu.itba.paw.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ProductDao {
    Product create(long categoryId, String name, String description, Long imageId, BigDecimal price);

    Optional<Product> getById(long productId);

    List<Product> getByCategory(long categoryId);

    void update(long productId, String name, BigDecimal price, String description);

    void delete(long productId);
}
