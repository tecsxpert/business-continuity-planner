package com.internship.tool.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;

@Service
public class AiServiceClient {

    private final RestTemplate restTemplate;
    private final String AI_URL = "http://localhost:5001";

    // Constructor with 10-second timeout
    public AiServiceClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(10000); // 10 seconds
        factory.setReadTimeout(10000);    // 10 seconds

        this.restTemplate = new RestTemplate(factory);
    }

    // Call /describe endpoint
    public String getDescription(String text) {
        String url = AI_URL + "/describe";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"text\": \"" + text + "\"}";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    request,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            return null; // required
        }
    }

    // Call /recommend endpoint
    public String getRecommendations(String text) {
        String url = AI_URL + "/recommend";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"text\": \"" + text + "\"}";

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    request,
                    String.class
            );

            return response.getBody();

        } catch (Exception e) {
            return null; // required
        }
    }
}