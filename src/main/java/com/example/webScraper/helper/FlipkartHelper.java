package com.example.webScraper.helper;

import com.example.webScraper.model.Product;
import com.example.webScraper.service.ScraperService;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class FlipkartHelper {
    private static final Logger log = LoggerFactory.getLogger(FlipkartHelper.class);

    public static List<Product> scrapeFromFlipkart(String keyword, ScraperService scraperService) {
        List<Product> products = new ArrayList<>();
        try {
            String searchUrl = "https://www.flipkart.com/search?q=" + URLEncoder.encode(keyword, StandardCharsets.UTF_8);
            Document doc = DocumentHelper.getDocument(searchUrl);
            //System.out.println(searchUrl);

            // Select all product containers
            Elements productContainers = doc.select("div.cPHDOP.col-12-12");
            for (Element container : productContainers) {
                try {
                    //System.out.println("******************************************************");

                    // Look for product link
                    Element productLinkElement = container.selectFirst("div.KzDlHZ");

                    if (productLinkElement == null) {
                        continue; // Not a product, skip
                    }

                    Product product = new Product();

                    // Set product name
                    String productName = productLinkElement.text();
                    if (productName.contains(keyword.toLowerCase())
                            && !(productName.contains("cover")|| productName.contains("edge to edge") || productName.contains("temp") || productName.contains("cov")   || productName.contains("case") || productName.contains("protector") || productName.contains("skin"))) {

                        product.setName(productName);

                        // Set product URL
                        Element urlElement = container.selectFirst("a.CGtC98");
                        if (urlElement != null) {
                            String productUrl = urlElement.attr("href");
                            if (!productUrl.startsWith("http")) {
                                productUrl = "https://www.flipkart.com" + productUrl;
                            }
                            product.setProductUrl(productUrl);
                        }

                        // Set price
                        Element priceElement = container.selectFirst("div.Nx9bqj._4b5DiR");
                        if (priceElement != null) {
                            String priceText = priceElement.text().replaceAll("[^0-9]", ""); // Remove ₹ , commas, etc.
                            if (!priceText.isEmpty()) {
                                Double price = Double.valueOf(priceText);
                                product.setPrice(price);
                            }
                        }

                        // Set product image
                        Element imgElement = container.selectFirst("img");
                        if (imgElement != null) {
                            String imgUrl = imgElement.attr("src");
                            if (imgUrl != null && !imgUrl.isEmpty()) {
                                product.setImageUrl(imgUrl);
                            }
                        }
                        product.setPlatform("Flipkart");
                        product.setCurrency("INR");
                        // Finally, add the product
                        products.add(product);
                    }

                } catch (Exception e) {
                    log.error("Error parsing a product", e);
                }
            }

        } catch (Exception e) {
            log.error("Error scraping Flipkart", e);
        }
        return products;
    }
}
