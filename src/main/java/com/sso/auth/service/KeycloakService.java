package com.sso.auth.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class KeycloakService {

    @Value("${keycloak.api.base-url}")
    private String apiBaseUrl;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-id}")
    private String apiCliId;

    @Value("${spring.security.oauth2.client.registration.keycloak.client-secret}")
    private String apiCliSecret;

    @Value("${spring.security.oauth2.client.provider.keycloak.token-uri}")
    private String apiTokenUrl;

    public String getToken() {
        String accessToken = "";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(apiTokenUrl);
            post.setHeader("Content-Type", "application/x-www-form-urlencoded");

            String body = "grant_type=client_credentials&client_id=" + apiCliId + "&client_secret=" + apiCliSecret;
            post.setEntity(new StringEntity(body));

            try(CloseableHttpResponse response = httpClient.execute(post)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(responseBody);

                    accessToken = jsonNode.get("access_token").asText();
                } else {
                    System.out.println(statusCode);
                }
            } 
        } catch (Exception e) {
            e.printStackTrace();
        }

        return accessToken;
    }

    public String getUserApplications(String userId, String accessToken) {
        String applications = "";

        String url = apiBaseUrl + "/myrealm/users/" + userId + "/role-mappings";

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet request = new HttpGet(url);
            request.setHeader("Authorization", "Bearer " + accessToken);

            try (CloseableHttpResponse response = httpClient.execute(request)) {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    String responseBody = EntityUtils.toString(response.getEntity());
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode jsonNode = mapper.readTree(responseBody);
                    JsonNode clientMappings = jsonNode.path("clientMappings");
                    applications = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(clientMappings);
                } else {
                    System.out.println(statusCode);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return applications;
    }
    
    public List<JsonNode> getClientsInfo(List<String> ids, String accessToken) {
        List<JsonNode> infos = new ArrayList<>();

        for (String id : ids) {
            String url = apiBaseUrl + "/myrealm/clients/" + id;

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpGet request = new HttpGet(url);
                request.setHeader("Authorization", "Bearer " + accessToken);

                try (CloseableHttpResponse response = httpClient.execute(request)) {
                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        String responseBody = EntityUtils.toString(response.getEntity());
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode jsonNode = mapper.readTree(responseBody);
                        infos.add(jsonNode);
                    } else {
                        System.out.println(statusCode);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return infos;
    }
}
