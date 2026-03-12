package com.billwise.repository;

import com.billwise.model.Profile;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfileRepository extends MongoRepository<Profile, String> {

    @Query("{ 'userId': ?0 }")
    List<Profile> findByUserId(String userId);

    Optional<Profile> findByEmail(String email);
}
