package com.sso.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        http
        .authorizeHttpRequests(authorize -> 
            authorize
            .requestMatchers("/login").permitAll()
            .requestMatchers("/dashboard", "/dashboardkc").authenticated()
            .anyRequest().authenticated()
        )
        .oauth2Login(oauth2Login ->
            oauth2Login
            .loginPage("/login")
            .successHandler((request, response, authentication) -> {

                String url = request.getRequestURL().toString();
                if (url.equals("http://192.168.0.59:18081/login/oauth2/code/okta")) {
                    response.sendRedirect("/dashboard");
                } else if (url.equals("http://192.168.0.59:18081/login/oauth2/code/keycloak")) {
                    response.sendRedirect("/dashboardkc");
                }
            })
            .failureHandler((request, response, exception) -> {
                System.err.println(exception.getMessage());
            })
            .userInfoEndpoint()
            .oidcUserService(this.oidcUserService())
        )
        .logout(oauth2Logout -> 
            oauth2Logout
            .logoutUrl("/logout")
            .logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository))
            .invalidateHttpSession(true)
            .clearAuthentication(true)
            .deleteCookies("JSESSIONID"));
            
        return http.build();
    }

    @Bean
    public OidcUserService oidcUserService() {
        OidcUserService delegate = new OidcUserService();

        return delegate;
    }
    
    private LogoutSuccessHandler oidcLogoutSuccessHandler(ClientRegistrationRepository clientRegistrationRepository) {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler = new OidcClientInitiatedLogoutSuccessHandler(clientRegistrationRepository);

        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("http://192.168.0.59:18081");
        
        return oidcLogoutSuccessHandler;
    }
}