package io.leiwang.springsecurityjpa;

import io.leiwang.springsecurityjpa.oauth2.AddCsrfToLoginSuccessResponseHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction.oauth2AuthorizedClient;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity(debug = true)
public class SecurityConfiguration {
    private static final String ADMIN_ROLE = "ADMIN";


    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        XorCsrfTokenRequestAttributeHandler delegate = new XorCsrfTokenRequestAttributeHandler();
        SimpleUrlAuthenticationFailureHandler failureHandler = new SimpleUrlAuthenticationFailureHandler("/");

        http.authorizeHttpRequests(
                        auth -> auth
                                .requestMatchers("/", "/index.html", "/error", "/webjars/**").permitAll()
                                .requestMatchers("/admin").hasRole(ADMIN_ROLE)
                                .requestMatchers("/user").hasAnyRole("USER", ADMIN_ROLE)
                                .requestMatchers("/users/me").hasAnyRole("USER", ADMIN_ROLE)
                                .anyRequest().authenticated()
                )
                .csrf(c -> c.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()).csrfTokenRequestHandler(delegate::handle))
                // disabling CSRF
                //.csrf(AbstractHttpConfigurer::disable)
                // success handler does not work...
                .oauth2Login(customizer -> customizer
                        .successHandler(new AddCsrfToLoginSuccessResponseHandler())
                        .failureHandler(((request, response, exception) -> {
                            request.getSession().setAttribute("error.message", exception.getMessage());
                            failureHandler.onAuthenticationFailure(request, response, exception);
                        }))
                )
                .formLogin(Customizer.withDefaults())
                .logout(l -> l.logoutSuccessUrl("/").permitAll())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                /*
                .sessionManagement(
                        sessionManagementConfigurer -> sessionManagementConfigurer.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )*/;
        return http.build();
    }

    // @Bean
    public PasswordEncoder getPasswordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> oauth2UserService(WebClient rest) {
        DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
        return request -> {
            OAuth2User user = delegate.loadUser(request);
            if (!"github".equals(request.getClientRegistration().getRegistrationId())) {
                return user;
            }

            OAuth2AuthorizedClient client = new OAuth2AuthorizedClient
                    (request.getClientRegistration(), user.getName(), request.getAccessToken());
            String url = user.getAttribute("organizations_url");
            List<Map<String, Object>> orgs = rest
                    .get().uri(url)
                    .attributes(oauth2AuthorizedClient(client))
                    .retrieve()
                    .bodyToMono(List.class)
                    .block();

            if (orgs.stream().anyMatch(org -> "spring-projects".equals(org.get("login")))) {
                return user;
            }

            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token", "Not in Spring Team", ""));
        };
    }

    @Bean
    public WebClient rest(ClientRegistrationRepository clients, OAuth2AuthorizedClientRepository authz) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(clients, authz);
        return WebClient.builder()
                .filter(oauth2).build();
    }
}
