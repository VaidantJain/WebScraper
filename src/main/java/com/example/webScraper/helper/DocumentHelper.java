package com.example.webScraper.helper;

import com.google.common.util.concurrent.RateLimiter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

public class DocumentHelper {
    private static final RateLimiter rateLimiter = RateLimiter.create(1.0);
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/107.0.0.0 Safari/537.36";

    protected static Document getDocument(String url) throws IOException {
        rateLimiter.acquire(); // respect rate limit

        int retries = 3;
        while (retries > 0) {
            try {
                return Jsoup.connect(url)
                        .userAgent(USER_AGENT)
                        .timeout(10000)
                        .header("Accept-Language", "en-US,en;q=0.9")
                        .header("Accept-Encoding", "gzip, deflate, br")
                        .header("Connection", "keep-alive")
                        .ignoreHttpErrors(true) // this will allow you to check response code
                        .get();
            } catch (IOException e) {
                retries--;
                if (retries == 0) {
                    throw e;
                }
                try {
                    Thread.sleep(2000); // wait before retry
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        throw new IOException("Failed to fetch document after retries");
    }

}
