package com.fiap.sus.network.core.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Collections;

@Configuration
public class RestTemplateConfig {

    /**
     * User-Agent customizado para requisições ao Nominatim (OpenStreetMap).
     * O Nominatim exige um User-Agent identificando a aplicação para evitar bloqueios.
     * Formato recomendado: "NomeApp/versão (contato@email.com)"
     */
    private static final String USER_AGENT = "SusConnect-Network-Service/1.0 (contact@fiap.edu.br)";

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        restTemplate.setInterceptors(Collections.singletonList(new UserAgentInterceptor()));
        
        return restTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    /**
     * Interceptor que adiciona User-Agent customizado em todas as requisições HTTP.
     */
    private static class UserAgentInterceptor implements ClientHttpRequestInterceptor {
        @Override
        public ClientHttpResponse intercept(
                HttpRequest request,
                byte[] body,
                ClientHttpRequestExecution execution) throws IOException {
            
            request.getHeaders().set("User-Agent", USER_AGENT);
            return execution.execute(request, body);
        }
    }
}
