package com.github.fenrir.xdashboard.config;

import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;

@Configuration
@EnableOpenApi
public class SwaggerConfig {
    private ApiInfo apiInfo(){
        return new ApiInfoBuilder()
                .title("XWatcher XDashboard")
                .description("The dashboard of XWatcher")
                .version("0.1")
                .build();
    }
}
