package com.example.webScraper.helper;
import com.example.webScraper.model.Product;
import com.example.webScraper.service.ScraperService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.print.Doc;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

public class CromaHelper {
    private static final Logger log = LoggerFactory.getLogger(CromaHelper.class);

    public static List<Product> scrapeFromCroma(String keyword, ScraperService scraperService) {
        List<Product> products = new ArrayList<>();
        try {
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String searchUrl = "https://www.croma.com/searchB?q=" + encodedKeyword + "%3Arelevance&text=" + encodedKeyword;
            System.out.println(searchUrl);
            Document doc = DocumentHelper.getDocument(searchUrl);
            System.out.println(doc.text());
            Elements productElements = doc.select("li.product-item");

            for (Element productElement : productElements) {
                System.out.println("in croma ***********************************************");
                try {
                    Product product = new Product();

                    // Extract product name
                    Element nameElement = productElement.select("h3.product-title").first();
                    if (nameElement != null) {
                        product.setName(nameElement.text());
                    } else {
                        continue; // Skip if no name found
                    }

                    // Extract product URL
                    Element linkElement = productElement.select("a.product-title").first();
                    if (linkElement != null) {
                        String productUrl = linkElement.attr("href");
                        if (!productUrl.startsWith("http")) {
                            productUrl = "https://www.croma.com" + productUrl;
                        }
                        product.setProductUrl(productUrl);
                    }

                    // Extract price
                    Element priceElement = productElement.select("span.amount").first();
                    if (priceElement != null) {
                        String priceText = priceElement.text().replaceAll("[₹,]", "").trim();
                        product.setPrice(Double.parseDouble(priceText));
                        product.setCurrency("INR");
                    }

                    // Extract image URL
                    Element imageElement = productElement.select("img.product-image").first();
                    if (imageElement != null) {
                        product.setImageUrl(imageElement.attr("src"));
                    }

                    // Set platform and time
                    product.setPlatform("Croma");
                    product.setScrapedAt(new Date());

                    products.add(scraperService.upsertProduct(product));
                } catch (Exception e) {
                    log.error("Error processing a Croma product element: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error fetching Croma search results: " + e.getMessage());
        }
        return products;
    }

    public static Product scrapeCromaProduct(String url) {
        try {
            Document doc = DocumentHelper.getDocument(url);
            Product product = new Product();

            // Extract product name
            product.setName(doc.select("h1.pdp-product-title").text());

            // Extract description
            product.setDescription(doc.select("div.pdp-desc-content").text());

            // Extract price
            String priceText = doc.select("span.new-price").text().replaceAll("[₹,]", "").trim();
            if (!priceText.isEmpty()) {
                product.setPrice(Double.parseDouble(priceText));
                product.setCurrency("INR");
            }

            // Extract image URL
            product.setImageUrl(doc.select("img.pdp-slider-img").attr("src"));

            product.setProductUrl(url);
            product.setPlatform("Croma");
            product.setScrapedAt(new Date());

            return product;
        } catch (IOException e) {
            log.error("Error fetching Croma product details: " + e.getMessage());
            return null;
        }
    }

}
