package com.sso.auth.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sso.auth.service.KeycloakService;

@Controller
@RequestMapping("/dashboardkc")
public class WebControllerKc {

    private final KeycloakService keycloakService;

    public WebControllerKc(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @GetMapping
    public String getDashboard(@AuthenticationPrincipal OidcUser user, Model model) {

        System.out.println("===============");
        System.out.println(user);
        String token = keycloakService.getToken();
        String applications = keycloakService.getUserApplications(user.getSubject(), token);
        if (user != null) {
            model.addAttribute("user", user);
            
            
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode rootNode = objectMapper.readTree(applications);
                List<String> ids = new ArrayList<>();
                Iterator<String> fieldNames = rootNode.fieldNames();
                while (fieldNames.hasNext()) {
                    String fieldName = fieldNames.next();
                    JsonNode clientNode = rootNode.get(fieldName);

                    String clientId = clientNode.get("id").asText();
                    ids.add(clientId);
                }
                
                List<JsonNode> clientsInfo = keycloakService.getClientsInfo(ids, token);

                model.addAttribute("applicationList", clientsInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        }

        return "dashboardkc";
    }
}
