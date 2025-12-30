package de.langen.beschlussservice.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class HttpClientConfig {

    @Bean
    public HttpClient httpClient(
            @Value("${application.http-client.connect-timeout}") int connectTimeout,
            @Value("${application.http-client.request-timeout}") int requestTimeout
    ) {
        return HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .build();
    }
}

