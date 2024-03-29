package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryService {

    Optional<Category> getById(long categoryId);

    Category getByIdChecked(long restaurantId, long categoryId, boolean allowDeleted);

    Category create(long restaurantId, String name);

    List<Category> getByRestaurantSortedByOrder(long restaurantId);

    Category updateCategory(long restaurantId, long categoryId, String name, Integer orderNum);

    Category updateName(long restaurantId, long categoryId, String name);

    void delete(long restaurantId, long categoryId);

    void setOrder(Category category, int orderNum);

    void moveProduct(long productId, long newCategoryId);
}
