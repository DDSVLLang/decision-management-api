package de.langen.decision_service.infrastructure.http.client;

public interface ExternalApiClient {
    <T> T get(String endpoint, Class<T> responseType);
    <T> T post(String endpoint, Object request, Class<T> responseType);
}

