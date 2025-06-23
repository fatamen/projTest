package tw.com.ispan.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import tw.com.ispan.domain.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByName(String name);
}