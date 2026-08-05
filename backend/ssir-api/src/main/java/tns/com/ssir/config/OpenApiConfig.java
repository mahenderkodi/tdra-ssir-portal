package tns.com.ssir.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        final String securitySchemeName = "bearerAuth";
        
        return new OpenAPI()
                // 1. Define Global API Metadata [1]
                .info(new Info()
                        .title("TDRA SMS Sender ID Registry (SSIR) API Portal")
                        .version("1.0.0")
                        .description("Production-grade, secure RESTful API specification for the SSIR Onboarding & Security Portal.")
                        .contact(new Contact()
                                .name("TDRA SSIR Support")
                                .email("support@ssir.gov.ae")
                                .url("https://ssir.gov.ae"))
                        .license(new License()
                                .name("TDRA Proprietary License")
                                .url("https://ssir.gov.ae/license")))
                
                // 2. Define Production Staging, and Local Server Gateways
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local Development Server"),
                        new Server().url("https://staging-api.ssir.gov.ae").description("Staging Server"),
                        new Server().url("https://api.ssir.gov.ae").description("Production Gateway")
                ))
                
                // 3. Inject Global JWT Security Requirements [1]
                .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
                .components(new Components()
                        .addSecuritySchemes(securitySchemeName,
                                new SecurityScheme()
                                        .name(securitySchemeName)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Please paste your secure JWT Access Token (excluding 'Bearer ') to authorize requests.")));
    }
}