package com.sso.auth.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sso.auth.service.OktaService;


@Controller
@RequestMapping("/dashboard")
public class WebController {

    private final OktaService oktaService;

    public WebController(OktaService oktaService) {
        this.oktaService = oktaService;
    }

    @GetMapping
    public String getDashboard(@AuthenticationPrincipal OidcUser user, Model model) {

        //Okta
        System.out.println("===============");
        System.out.println(user);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("applications", oktaService.getUserApplications(user.getName()));
        }

        String applicationList = model.getAttribute("applications").toString();
        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // JSON 배열 문자열을 List<JsonNode>로 변환
            List<JsonNode> jsonNodeList = objectMapper.readValue(applicationList, new TypeReference<List<JsonNode>>() {
            });
        
            model.addAttribute("applicationList", jsonNodeList);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "dashboard";
    }
}
