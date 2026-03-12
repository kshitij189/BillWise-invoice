package com.billwise.repository;

import com.billwise.model.Client;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends MongoRepository<Client, String> {

    @Query("{ 'userId': ?0 }")
    Page<Client> findByUserId(String userId, Pageable pageable);

    @Query("{ 'userId': ?0 }")
    List<Client> findByUserId(String userId);
}
