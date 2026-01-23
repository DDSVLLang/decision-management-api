package de.langen.decision_service.infrastructure.http.adapter;


import com.fasterxml.jackson.databind.ObjectMapper;
import de.langen.decision_service.infrastructure.http.client.ExternalApiClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
@Slf4j
public class ExternalApiAdapter implements ExternalApiClient {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final String baseUrl;
    private final int maxRetries;

    public ExternalApiAdapter(
            HttpClient httpClient,
            ObjectMapper objectMapper,
            @Value("${application.http-client.base-url}") String baseUrl,
            @Value("${application.http-client.max-retries}") int maxRetries
    ) {
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.baseUrl = baseUrl;
        this.maxRetries = maxRetries;
    }

    @Override
    public <T> T get(String endpoint, Class<T> responseType) {
        log.debug("HTTP GET: {}{}", baseUrl, endpoint);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + endpoint))
                .GET()
                .header("Content-Type", "application/json")
                .build();

        return executeWithRetry(request, responseType);
    }

    @Override
    public <T> T post(String endpoint, Object requestBody, Class<T> responseType) {
        log.debug("HTTP POST: {}{}", baseUrl, endpoint);

        try {
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + endpoint))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .header("Content-Type", "application/json")
                    .build();

            return executeWithRetry(request, responseType);

        } catch (Exception e) {
            log.error("Failed to serialize request body", e);
            throw new RuntimeException("HTTP request failed", e);
        }
    }

    private <T> T executeWithRetry(HttpRequest request, Class<T> responseType) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            try {
                HttpResponse<String> response = httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

                if (response.statusCode() >= 200 && response.statusCode() < 300) {
                    return objectMapper.readValue(response.body(), responseType);
                } else if (response.statusCode() >= 500 && attempt < maxRetries - 1) {
                    log.warn("Server error ({}), retrying... attempt {}/{}",
                            response.statusCode(), attempt + 1, maxRetries);
                    attempt++;
                    Thread.sleep(1000L * attempt); // Exponential backoff
                    continue;
                } else {
                    throw new RuntimeException(
                            "HTTP error " + response.statusCode() + ": " + response.body()
                    );
                }

            } catch (Exception e) {
                log.error("HTTP request failed, attempt {}/{}", attempt + 1, maxRetries, e);
                lastException = e;
                attempt++;

                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(1000L * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Request interrupted", ie);
                    }
                }
            }
        }

        throw new RuntimeException("HTTP request failed after " + maxRetries + " retries", lastException);
    }
}
