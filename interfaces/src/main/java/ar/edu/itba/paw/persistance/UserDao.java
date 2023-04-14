package ar.edu.itba.paw.persistance;

import ar.edu.itba.paw.model.User;

import java.util.Optional;

public interface UserDao {
    User create(String username, String password, String email);

    Optional<User> getById(long userId);
}
