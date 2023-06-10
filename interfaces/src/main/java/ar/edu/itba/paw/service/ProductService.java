package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Product;
import ar.edu.itba.paw.model.Promotion;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ProductService {

    Optional<Product> getById(long productId);

    Product create(long categoryId, String name, String description, byte[] image, BigDecimal price);

    Product update(long productId, String name, BigDecimal price, String description);

    void updateImage(long productId, byte[] image);

    void delete(long productId);

    Promotion createPromotion(long sourceProductId, LocalDateTime startDate, LocalDateTime endDate, float discount);
}
