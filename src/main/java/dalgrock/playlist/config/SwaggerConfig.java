package dalgrock.playlist.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        String cookieAuthName = "cookieAuth";

        return new OpenAPI()
                .info(new Info()
                        .title("Pliview API")
                        .description("Pliview API 문서")
                        .version("1.0.0"))
                .components(new Components()
                        .addSecuritySchemes(cookieAuthName,
                                new SecurityScheme()
                                        .name("access_token")
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)));
    }

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            String controllerName = handlerMethod.getBeanType().getSimpleName()
                    .replace("Controller", "")
                    .toLowerCase();

            String methodName = handlerMethod.getMethod().getName();

            operation.setOperationId(controllerName + "_" + methodName);

            return operation;
        };
    }
}
