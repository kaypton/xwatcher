package com.github.fenrir.xregistry.configs;

import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;

@Configuration
@EnableOpenApi
public class SwaggerConfig {
    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("XWatcher XRegistry")
                .description("The registry of XWatcher")
                .version("0.1")
                .build();
    }
}
