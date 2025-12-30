package de.langen.beschlussservice.infrastructure.http.client;

public interface ExternalApiClient {
    <T> T get(String endpoint, Class<T> responseType);
    <T> T post(String endpoint, Object request, Class<T> responseType);
}

