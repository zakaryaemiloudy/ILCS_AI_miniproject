package com.estn.ilcsagentai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {

    // Force HTTP/1.1 to avoid RST_STREAM errors with Groq API on Java 21+
    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .protocol(HttpProtocol.HTTP11);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    // Separate WebClient for Groq TTS (Orpheus) — 10MB buffer for audio
    @Bean("ttsWebClient")
    public WebClient ttsWebClient() {
        HttpClient httpClient = HttpClient.create()
                .protocol(HttpProtocol.HTTP11);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .codecs(config -> config.defaultCodecs().maxInMemorySize(10 * 1024 * 1024))
                .baseUrl("https://api.groq.com/openai")
                .build();
    }
}
