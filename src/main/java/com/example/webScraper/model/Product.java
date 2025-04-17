package com.example.webScraper.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "products")

@Data
@Builder @AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    private String id;
    @Indexed(unique = true)
    private String name;
    private String description;
    private Double price;
    private String currency;
    private String imageUrl;
    @Indexed(unique = true)
    private String productUrl;
    private String platform;
    private Date scrapedAt;
}
