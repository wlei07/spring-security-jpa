package filters;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@RequiredArgsConstructor
public class FilterRegistrationConfiguration {
    private final ApplicationContext applicationContext;

    @Bean
    FilterRegistrationBean<MyCustomHeaderFilter> myCustomHeaderFilterFilterRegistrationBean(MyCustomHeaderFilter myCustomHeaderFilter) {
        FilterRegistrationBean<MyCustomHeaderFilter> registrationBean = new FilterRegistrationBean<>(myCustomHeaderFilter);
        if (applicationContext.containsBean("headersLoggingFilter")) {
            registrationBean.setOrder(-1);
        }
        registrationBean.setOrder(Ordered.LOWEST_PRECEDENCE);
        return registrationBean;
    }

    @Bean
    FilterRegistrationBean<HeadersLoggingFilter> registrationBean() {
        final FilterRegistrationBean<HeadersLoggingFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new HeadersLoggingFilter());
        registrationBean.setOrder(-1);
        registrationBean.setName("fooBar");
        //registrationBean.setUrlPatterns(Collections.singletonList("/test/*"));
        return registrationBean;
    }
}
