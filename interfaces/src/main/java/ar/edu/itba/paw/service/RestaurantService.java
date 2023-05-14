package ar.edu.itba.paw.service;

import ar.edu.itba.paw.model.*;
import ar.edu.itba.paw.util.PaginatedResult;
import ar.edu.itba.paw.util.Pair;

import java.util.List;
import java.util.Optional;

public interface RestaurantService {
    long create(String name, String email, int specialty, long ownerUserId, String description, String address, int maxTables, byte[] logo, byte[] portrait1, byte[] portrait2);

    Optional<Restaurant> getById(long restaurantId);

    PaginatedResult<Restaurant> getActive(int pageNumber, int pageSize);

    int getActiveCount();

    PaginatedResult<Restaurant> getSearchResults(String query, int pageNumber, int pageSize);

    PaginatedResult<Restaurant> getSortedByNameAsc(int pageNumber, int pageSize);

    PaginatedResult<Restaurant> getSortedByNameDesc(int pageNumber, int pageSize);

    PaginatedResult<Restaurant> getSortedByPriceAverageAsc(int pageNumber, int pageSize);

    PaginatedResult<Restaurant> getSortedByPriceAverageDesc(int pageNumber, int pageSize);

    PaginatedResult<Restaurant> getSortedByCreationDateAsc(int pageNumber, int pageSize);

    PaginatedResult<Restaurant> getSortedByCreationDateDesc(int pageNumber, int pageSize);

    PaginatedResult<Restaurant> getSortedByAveragePriceAsc(int pageNumber, int pageSize);

    PaginatedResult<Restaurant> getSortedByAveragePriceDesc(int pageNumber, int pageSize);

    List<Pair<Restaurant, Integer>> getAverageRatingForRestaurants(List<Restaurant> restaurants);

    List<Pair<Category, List<Product>>> getMenu(long restaurantId);

    boolean delete(long restaurantId);

    List<RestaurantTags> getTags(long restaurantId);

    boolean addTag(long restaurantId, long tagId);

    boolean removeTag(long restaurantId, long tagId);
}
