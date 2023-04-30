package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.model.util.Pair;

import java.util.List;
import java.util.Optional;

public interface RestaurantService {
    Restaurant create(String name, String email);

    Optional<Restaurant> getById(int restaurantId);

    List<Restaurant> getActive(int pageNumber, int pageSize);

    int getActiveCount();

    List<Restaurant> getSearchResults(String query, int pageNumber, int pageSize);

    int getSearchResultsCount(String query);

    List<Pair<Category, List<Product>>> getMenu(int restaurantId);

    boolean delete(int restaurantId);
}
