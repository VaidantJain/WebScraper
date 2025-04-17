package com.example.webScraper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.StandardException;

import java.util.List;
@Data @NoArgsConstructor @AllArgsConstructor@Builder
public class PriceComparison {
    private String productName;
    private List<Product> prices;


}