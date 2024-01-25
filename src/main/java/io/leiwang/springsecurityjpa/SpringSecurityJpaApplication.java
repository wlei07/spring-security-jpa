package io.leiwang.springsecurityjpa;

import filters.FilterComponentPackage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackageClasses = {FilterComponentPackage.class, SpringSecurityJpaApplication.class})
@EnableJpaRepositories(basePackageClasses = UserRepository.class)
public class SpringSecurityJpaApplication {
    public static void main(String[] args) {
        SpringApplication.run(SpringSecurityJpaApplication.class, args);
    }
}
