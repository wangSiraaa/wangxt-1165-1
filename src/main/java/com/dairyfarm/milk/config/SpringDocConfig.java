package com.dairyfarm.milk.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringDocConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("奶牛场生鲜乳抗生素检测系统API")
                        .description("牧场、化验室、司机三方协作的生鲜乳质量管控系统")
                        .contact(new Contact().name("dairyfarm"))
                        .version("1.0.0"));
    }
}
