package org.dspace.app.rest.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                        .allowedOrigins("http://localhost:4000") // your Angular app
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                        .allowedHeaders(
                                "Accept",
                                "Authorization",
                                "Content-Type",
                                "Origin",
                                "X-On-Behalf-Of",
                                "X-Requested-With",
                                "X-XSRF-TOKEN",
                                "X-CORRELATION-ID",
                                "X-REFERRER",
                                "Cache-Control"
                        )
                        .exposedHeaders(
                                "Authorization",
                                "DSPACE-XSRF"
                        )
                        .allowCredentials(true);
            }
        };
    }
}
