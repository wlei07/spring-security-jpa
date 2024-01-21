package io.leiwang.springsecurityjpa.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
public class SocialController {
    @GetMapping("/user-oauth2")
    public Map<String, Object> user(@AuthenticationPrincipal OAuth2User principal) {
        return Collections.singletonMap("name", getUserName(principal));
    }

    @GetMapping("/error")
    public String error(HttpServletRequest request) {
        String message = (String) request.getSession().getAttribute("error.message");
        request.getSession().removeAttribute("error.message");
        return message;
    }

    private static String getUserName(OAuth2User principal) {
        // GitHub
        if (principal.getAttribute("login") != null) {
            return principal.getAttribute("login");
        }
        // Google
        else {
            return principal.getAttribute("name");
        }
    }
}
