package com.example.webScraper.service;

import com.example.webScraper.helper.*;
import com.example.webScraper.model.PriceComparison;
import com.example.webScraper.model.Product;
import com.example.webScraper.repository.ProductRepository;
import com.google.common.util.concurrent.RateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ScraperService {

    @Autowired
    private ProductRepository productRepository;
    private static final Logger log = LoggerFactory.getLogger(ScraperService.class);

    public List<Product> scrapeByKeyword(String keyword) throws Exception {
        CompletableFuture<List<Product>> amazonFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return AmazonHelper.scrapeFromAmazon(keyword, this);
            } catch (Exception e) {
                log.error("Amazon scraping failed: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<Product>> flipkartFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return FlipkartHelper.scrapeFromFlipkart(keyword, this);
            } catch (Exception e) {
                log.error("Flipkart scraping failed: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        CompletableFuture<List<Product>> shopCluesFuture = CompletableFuture.supplyAsync(() -> {
            try {
                return ShopCluesHelper.scrapeFromShopClues(keyword, this);
            } catch (Exception e) {
                log.error("ShopClues scraping failed: {}", e.getMessage());
                return new ArrayList<>();
            }
        });

        // Wait for all to complete
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(amazonFuture, flipkartFuture, shopCluesFuture);

        // Combine results
        CompletableFuture<List<Product>> allProductsFuture = allFutures.thenApply(v -> {
            List<Product> allProducts = new ArrayList<>();
            try {
                allProducts.addAll(amazonFuture.get());
                allProducts.addAll(flipkartFuture.get());
                allProducts.addAll(shopCluesFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error combining scraped results: {}", e.getMessage());
            }
            return allProducts;
        });

        // Save all products to DB
        return productRepository.saveAll(allProductsFuture.get());
    }


    @ExceptionHandler(IOException.class)
    public void handleIOException(IOException e) {
        log.error("Error scraping: " + e.getMessage());
    }

    public Product upsertProduct(Product scrapedProduct) {
        Optional<Product> existingProduct = productRepository.findByProductUrlAndPlatform(
                scrapedProduct.getProductUrl(),
                scrapedProduct.getPlatform()
        );

        if (existingProduct.isPresent()) {
            // Update existing product details
            Product product = existingProduct.get();
            product.setName(scrapedProduct.getName());
            product.setDescription(scrapedProduct.getDescription());
            product.setPrice(scrapedProduct.getPrice());
            product.setCurrency(scrapedProduct.getCurrency());
            product.setImageUrl(scrapedProduct.getImageUrl());
            product.setScrapedAt(new Date());
            return productRepository.save(product);
        } else {
            // Insert new product
            scrapedProduct.setScrapedAt(new Date());
            return productRepository.save(scrapedProduct);
        }
    }

    public List<PriceComparison> compareProductPrices(String productName) throws Exception {
        List<Product> allProducts = scrapeByKeyword(productName);
        Map<String, List<Product>> platformProducts = allProducts.stream()
                .collect(Collectors.groupingBy(Product::getPlatform));

        List<PriceComparison> comparisons = new ArrayList<>();

        // Group similar products
        Map<String, List<Product>> similarProducts = groupSimilarProducts(allProducts);

        for (Map.Entry<String, List<Product>> entry : similarProducts.entrySet()) {
            PriceComparison comparison = new PriceComparison();
            comparison.setProductName(entry.getKey());

            List<Product> prices = new ArrayList<>();
            for (Product product : entry.getValue()) {
                Product price = new Product();
                price.setPlatform(product.getPlatform());
                price.setPrice(product.getPrice());
                price.setCurrency(product.getCurrency());
                price.setProductUrl(product.getProductUrl());
                prices.add(price);
            }

            // Sort by price (lowest first)
            prices.sort(Comparator.comparing(Product::getPrice));
            comparison.setPrices(prices);
            comparisons.add(comparison);
        }

        return comparisons;
    }

    private Map<String, List<Product>> groupSimilarProducts(List<Product> products) {
        // This is a simplistic approach - in a real-world scenario, you'd want to use
        // more sophisticated text similarity algorithms or product identifiers
        Map<String, List<Product>> groupedProducts = new HashMap<>();

        for (Product product : products) {
            String simplifiedName = simplifyProductName(product.getName());
            groupedProducts.computeIfAbsent(simplifiedName, k -> new ArrayList<>()).add(product);
        }

        return groupedProducts;
    }

    private String simplifyProductName(String name) {
        // Remove common words, numbers, special characters
        // Convert to lowercase
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\b(the|a|an|and|or|with|for|in|on|at|to)\\b", "")
                .trim()
                .replaceAll("\\s+", " ");
    }

}
