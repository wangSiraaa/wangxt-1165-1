package com.dairyfarm.milk.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtAuthenticationFilter)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/auth/login",
                        "/doc.html/**", "/swagger-resources/**",
                        "/v2/api-docs/**", "/v3/api-docs/**",
                        "/webjars/**", "/swagger-ui/**", "/swagger-ui.html",
                        "/favicon.ico",
                        "/", "/index.html", "/static/**",
                        "/css/**", "/js/**", "/fonts/**", "/img/**", "/images/**"
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/static/");
        registry.addResourceHandler("/css/**", "/js/**", "/fonts/**", "/img/**", "/images/**")
                .addResourceLocations("classpath:/static/static/css/",
                        "classpath:/static/static/js/",
                        "classpath:/static/static/fonts/",
                        "classpath:/static/static/img/",
                        "classpath:/static/static/images/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
