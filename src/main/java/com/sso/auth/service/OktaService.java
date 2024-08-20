package com.sso.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class OktaService {

    private final RestTemplate restTemplate;

    @Value("${okta.api.token}")
    private String apiToken;

    @Value("${okta.api.base-url}")
    private String apiBaseUrl;

    public OktaService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getUserApplications(String userId) {
        String url = apiBaseUrl + "/api/v1/apps?filter=user.id eq \"" + userId + "\"&expand=user/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SSWS " + apiToken);
        headers.set("Accept", "application/json");
        headers.set("Content-Type", "application/json");
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> userResponse = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        return userResponse.getBody();
    }
}