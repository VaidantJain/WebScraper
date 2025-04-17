package com.example.webScraper.controller;

import com.example.webScraper.model.Product;
import com.example.webScraper.service.ScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    @Autowired
    private ScraperService scraperService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard"; // renders dashboard.html via Thymeleaf
    }

    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<?> search(
            @RequestParam("query") String query,
            @RequestParam(value = "sortOption", required = false) String sortOption,
            @RequestParam(value = "platform", required = false) String platformFilter
    ) {
        try {
            List<Product> products = scraperService.scrapeByKeyword(query);

            // Filter by platform if provided
            if (platformFilter != null && !platformFilter.isEmpty()) {
                products = products.stream()
                        .filter(p -> p.getPlatform().equalsIgnoreCase(platformFilter))
                        .collect(Collectors.toList());
            }

            // Sort based on sortOption if provided
            if (sortOption != null && !sortOption.isEmpty()) {
                switch (sortOption) {
                    case "price_asc":
                        products.sort(Comparator.comparing(Product::getPrice));
                        break;
                    case "price_desc":
                        products.sort(Comparator.comparing(Product::getPrice).reversed());
                        break;
                    case "name_asc":
                        products.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER));
                        break;
                    case "name_desc":
                        products.sort(Comparator.comparing(Product::getName, String.CASE_INSENSITIVE_ORDER).reversed());
                        break;
                    default:
                        // no sorting if invalid option
                        break;
                }
            }

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Search failed: " + e.getMessage()));
        }
    }
}
