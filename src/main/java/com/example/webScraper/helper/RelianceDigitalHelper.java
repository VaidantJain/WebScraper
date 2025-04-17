package com.example.webScraper.helper;

import com.example.webScraper.model.Product;
import com.example.webScraper.service.ScraperService;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.StringReader;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RelianceDigitalHelper {
    private static final Logger log = LoggerFactory.getLogger(RelianceDigitalHelper.class);

    public static List<Product> scrapeFromRelianceDigital(String keyword, ScraperService scraperService) {
        List<Product> products = new ArrayList<>();
        try {
            // Encode the search keyword
            String encodedKeyword = URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            String searchUrl = "https://www.reliancedigital.in/products?q=" + encodedKeyword + "&page_no=1&page_size=12&page_type=number";

            log.info("Fetching URL: {}", searchUrl);
            System.out.println("Debug: Connecting to URL - " + searchUrl);

            // Fetch the HTML document using Jsoup
            Document doc = Jsoup.connect(searchUrl).get();
            String htmlContent = doc.html();
            System.out.println("Debug: HTML content length - " + htmlContent.length());

            // Extract JSON data using regex
            String regex = "__INITIAL_STATE__=(\\{.*?\\});";
            java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.DOTALL);
            java.util.regex.Matcher matcher = pattern.matcher(htmlContent);

            if (matcher.find()) {
                String jsonData = matcher.group(1);
                System.out.println("Debug: JSON data length - " + jsonData.length());

                // Use lenient parsing for malformed JSON
                JsonReader reader = new JsonReader(new StringReader(jsonData));
                reader.setLenient(true);

                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                System.out.println("Debug: JSON parsed successfully");

                JsonObject productListingPage = jsonObject.getAsJsonObject("productListingPage");
                JsonObject productLists = productListingPage.getAsJsonObject("productlists");
                JsonArray items = productLists.getAsJsonArray("items");

                System.out.println("Debug: Found " + items.size() + " items");

                // Iterate over each product item
                for (JsonElement item : items) {
                    System.out.println("\nDebug: Processing item ------------------------");
                    try {
                        JsonObject productJson = item.getAsJsonObject();
                        Product product = new Product();

                        // Extract product name
                        String name = productJson.get("name").getAsString();
                        product.setName(name);
                        System.out.println("Debug: Product name - " + name);

                        // Extract product URL
                        String urlPath = productJson.get("url").getAsString();
                        String productUrl = "https://www.reliancedigital.in" + urlPath;
                        product.setProductUrl(productUrl);
                        System.out.println("Debug: Product URL - " + productUrl);

                        // Extract price
                        JsonObject priceObj = productJson.getAsJsonObject("price").getAsJsonObject("effective");
                        double price = priceObj.get("min").getAsDouble();
                        String currency = priceObj.get("currency_code").getAsString();
                        product.setPrice(price);
                        product.setCurrency(currency);
                        System.out.println("Debug: Price - " + currency + " " + price);

                        // Extract image URL
                        JsonArray medias = productJson.getAsJsonArray("medias");
                        if (medias != null && medias.size() > 0) {
                            String imageUrl = medias.get(0).getAsJsonObject().get("url").getAsString();
                            product.setImageUrl(imageUrl);
                            System.out.println("Debug: Image URL - " + imageUrl);
                        }

                        // Set platform and timestamp
                        product.setPlatform("Reliance Digital");
                        product.setScrapedAt(new Date());

                        // Save or update the product using ScraperService
                        products.add(scraperService.upsertProduct(product));
                        System.out.println("Debug: Product added successfully");
                    } catch (Exception e) {
                        System.out.println("Error processing item: " + e.getMessage());
                        log.error("Error processing a Reliance Digital product element: {}", e.getMessage());
                    }
                }
            } else {
                System.out.println("Debug: No __INITIAL_STATE__ found in HTML");
                log.warn("No __INITIAL_STATE__ found in page source.");
            }
        } catch (Exception e) {
            System.out.println("Debug: General error - " + e.getMessage());
            log.error("Error fetching Reliance Digital search results: {}", e.getMessage());
        }
        return products;
    }

    public static Product scrapeRelianceDigitalProduct(String url) {
        try {
            Document doc = Jsoup.connect(url).get();
            Product product = new Product();

            // Extract product name
            String name = doc.select("h1.pdp__title").text();
            product.setName(name);
            System.out.println("Debug: Product name - " + name);

            // Extract description
            String description = doc.select("div.pdp__tab-content").text();
            product.setDescription(description);
            System.out.println("Debug: Description - " + description);

            // Extract price
            String priceText = doc.select("span.pdp__price").text().replaceAll("[₹,]", "").trim();
            if (!priceText.isEmpty()) {
                double price = Double.parseDouble(priceText);
                product.setPrice(price);
                product.setCurrency("INR");
                System.out.println("Debug: Price - INR " + price);
            }

            // Extract image URL
            String imageUrl = doc.select("img.pdp__image").attr("src");
            if (!imageUrl.isEmpty()) {
                product.setImageUrl(imageUrl);
                System.out.println("Debug: Image URL - " + imageUrl);
            }

            // Set other details
            product.setProductUrl(url);
            product.setPlatform("Reliance Digital");
            product.setScrapedAt(new Date());

            return product;
        } catch (Exception e) {
            System.out.println("Debug: Error fetching individual product details - " + e.getMessage());
            log.error("Error fetching Reliance Digital product details: {}", e.getMessage());
        }
        return null;
    }
}
