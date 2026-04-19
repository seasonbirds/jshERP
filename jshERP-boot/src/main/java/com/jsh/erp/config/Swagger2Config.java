package com.jsh.erp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 插件集成配置
 *
 * @author jishenghua
 * @version 1.0
 */
@Configuration
public class Swagger2Config {

    @Bean
    public OpenAPI createRestApi() {
        return new OpenAPI()
                .info(this.apiInfo());
    }

    private Info apiInfo() {
        return new Info()
                .title("管伊佳ERP Restful Api")
                .description("管伊佳ERP接口描述")
                .termsOfService("http://127.0.0.1")
                .contact(new Contact().name("jishenghua"))
                .version("3.0");
    }

}