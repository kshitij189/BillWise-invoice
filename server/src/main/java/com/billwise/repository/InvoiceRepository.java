package com.billwise.repository;

import com.billwise.model.Invoice;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends MongoRepository<Invoice, String> {

    @Query("{ 'creator': ?0 }")
    List<Invoice> findByCreator(String creator);

    @Query(value = "{ 'creator': ?0 }", count = true)
    long countByCreator(String creator);
}
