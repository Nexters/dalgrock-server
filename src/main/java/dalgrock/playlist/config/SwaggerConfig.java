package dalgrock.playlist.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Value("${app.api.server-url:}")
    private String serverUrl;

    @Bean
    public OpenAPI openAPI() {
        String cookieAuthName = "cookieAuth";

        OpenAPI openAPI = new OpenAPI()
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

        if (serverUrl != null && !serverUrl.isBlank()) {
            openAPI.addServersItem(new Server().url(serverUrl).description("API Server"));
        }
        return openAPI;
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
