package filters;

import jakarta.annotation.Nonnull;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
public class HeadersLoggingFilter implements Filter {
//    @Override
    public void doFilterInternal(@Nonnull HttpServletRequest servletRequest, @Nonnull HttpServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        Collections.list(servletRequest.getHeaderNames()).forEach(header -> log.info("Header: {}={}", header, servletRequest.getHeader(header)));
        filterChain.doFilter(servletRequest, servletResponse);
        log.info("after do filter#0001");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest servletRequest = (HttpServletRequest)request;
        Collections.list(servletRequest.getHeaderNames()).forEach(header -> log.info("Header: {}={}", header, servletRequest.getHeader(header)));
        chain.doFilter(servletRequest, response);
        log.info("after do filter#0001");
    }
}
