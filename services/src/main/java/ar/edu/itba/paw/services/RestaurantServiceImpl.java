package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Restaurant;
import ar.edu.itba.paw.persistance.RestaurantDao;
import ar.edu.itba.paw.service.RestaurantService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class RestaurantServiceImpl implements RestaurantService {

    private final RestaurantDao restaurantDao;

    @Autowired
    public RestaurantServiceImpl(final RestaurantDao RestaurantDao) {
        this.restaurantDao = RestaurantDao;
    }

    @Override
    public Optional<Restaurant> getById(long restaurantId) {
        return restaurantDao.getById(restaurantId);
    }

    @Override
    public Restaurant create(String name) {
        return restaurantDao.create(name);
    }

    @Override
    public boolean delete(long restaurantId) {
        return restaurantDao.delete(restaurantId);
    }
}