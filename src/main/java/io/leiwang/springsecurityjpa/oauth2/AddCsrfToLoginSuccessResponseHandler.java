package io.leiwang.springsecurityjpa.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.csrf.CsrfToken;

import java.io.IOException;

public class AddCsrfToLoginSuccessResponseHandler extends SavedRequestAwareAuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws ServletException, IOException {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        csrfToken.getToken(); // causes the deferred token to be rendered to the response when using CookieCsrfTokenRepository
        super.onAuthenticationSuccess(request, response, authentication);
    }
}
