package com.example.webScraper.repository;

import com.example.webScraper.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<Product, String> {

    Product save(Product product);

    List<Product> findByPlatform(String platform);
    Page<Product> findByPlatform(String platform, Pageable pageable);
    Optional<Product> findByProductUrlAndPlatform(String productUrl, String platform);
}
