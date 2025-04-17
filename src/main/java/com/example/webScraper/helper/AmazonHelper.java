package com.example.webScraper.helper;

import com.example.webScraper.model.Product;
import com.example.webScraper.service.ScraperService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AmazonHelper {
    private static final Logger log = LoggerFactory.getLogger(AmazonHelper.class);
    public static List<Product> scrapeFromAmazon(String keyword, ScraperService scraperService) {
        List<Product> products = new ArrayList<>();
        try {
            String searchUrl = "https://www.amazon.in/s?k=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            //System.out.println(searchUrl);
            Document doc = DocumentHelper.getDocument(searchUrl);
            Elements productElements = doc.select("div.s-result-item[data-component-type=s-search-result]");

            for (Element productElement : productElements) {
                try {
                    Product product = new Product();
                    String relativeUrl = productElement.select("a.a-link-normal").attr("href");
                    String fullUrl = "https://www.amazon.in" + relativeUrl;


                    // Set Name
                    product.setName(productElement.select("h2 span").text());

                    // Set Product URL
                    product.setProductUrl(fullUrl);

                    // Scrape and set Price
                    String wholePart = productElement.select("span.a-price-whole").text().replace(",", "").trim();
                    String fractionPart = productElement.select("span.a-price-fraction").text().trim();
                    if (!wholePart.isEmpty()) {
                        String fullPrice = wholePart + "." + (fractionPart.isEmpty() ? "00" : fractionPart);
                        product.setPrice(Double.parseDouble(fullPrice));
                        product.setCurrency("INR"); // Because you're scraping from amazon.in
                    }

                    // Set Image URL
                    product.setImageUrl(productElement.select("img.s-image").attr("src"));

                    // Set Platform and Scraped Time
                    product.setPlatform("Amazon");
                    product.setScrapedAt(new Date());

                    // Add to list
                    products.add(scraperService.upsertProduct(product));
                } catch (Exception e) {
                    log.error("Error processing a product element: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            log.error("Error fetching Amazon search results: " + e.getMessage());
        }
        return products;
    }

    public static Product scrapeAmazonProduct(String url) {
        try {
            Document doc = DocumentHelper.getDocument(url);
            Product product = new Product();
            product.setName(doc.select("#productTitle").text());
            product.setDescription(doc.select("#productDescription p").text());

            String priceText = doc.select(".a-price .a-offscreen").text();
            if (!priceText.isEmpty()) {
                priceText = priceText.replace("$", "").replace(",", "");
                product.setPrice(Double.parseDouble(priceText));
                product.setCurrency("USD");
            }

            product.setImageUrl(doc.select("#landingImage").attr("src"));
            product.setProductUrl(url);
            product.setPlatform("Amazon");
            product.setScrapedAt(new Date());

            return product;
        } catch (IOException e) {
            log.error("Error fetching Amazon product details: " + e.getMessage());
            return null;
        }
    }
}
