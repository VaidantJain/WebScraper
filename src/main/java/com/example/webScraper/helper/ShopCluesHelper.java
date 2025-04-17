package com.example.webScraper.helper;

import com.example.webScraper.model.Product;
import com.example.webScraper.service.ScraperService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.PrintStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ShopCluesHelper {
    private static final Logger log = LoggerFactory.getLogger(ShopCluesHelper.class);

    public static List<Product> scrapeFromShopClues(String keyword, ScraperService scraperService) {
        List<Product> products = new ArrayList<>();
        try {
            String searchUrl = "https://www.shopclues.com/search?q=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            //System.out.println(searchUrl);
            Document doc = DocumentHelper.getDocument(searchUrl);
            //System.out.println(doc.html());

            Elements productElements = doc.select("div.column");

            for (Element productElement : productElements) {
                try {
                    Product product = new Product();

                    // Set Name
                    String productName = productElement.select("h2").text().toLowerCase();
                    if (productName.contains(keyword.toLowerCase())
                            && !(productName.contains("cover")|| productName.contains("edge to edge") || productName.contains("temp") || productName.contains("cov")   || productName.contains("case") || productName.contains("protector") || productName.contains("skin"))) {

                            product.setName(productElement.select("h2").text());
                        //System.out.println(product.getName());

                        // Set Product URL
                        String productUrl = productElement.select("a").attr("href");
                        if (!productUrl.startsWith("http")) {
                            productUrl = "https:" + productUrl; // ShopClues sometimes gives relative URLs
                        }
                        product.setProductUrl(productUrl);
                        //System.out.println(product.getProductUrl());

                        // Scrape and Set Price
                        String priceText = productElement.select(".p_price").text().replace("₹", "").replace(",", "").trim();
                        if (!priceText.isEmpty()) {
                            product.setPrice(Double.parseDouble(priceText));
                            product.setCurrency("INR");
                            //System.out.println(product.getPrice());
                        }

                        // Set Image URL
                        product.setImageUrl(productElement.select("div.img_section img").attr("src"));
                        //System.out.println(product.getImageUrl());
                        // Set Platform and Scraped Time
                        product.setPlatform("ShopClues");
                        product.setScrapedAt(new Date());

                        // Add to list
                        if (product.getName() != null && !product.getName().isEmpty()) {
                            products.add(scraperService.upsertProduct(product));
                        }
                    }
                } catch (Exception e) {
                    log.error("Error processing a product element: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error fetching ShopClues page: " + e.getMessage());
        }
        return products;
    }
}
