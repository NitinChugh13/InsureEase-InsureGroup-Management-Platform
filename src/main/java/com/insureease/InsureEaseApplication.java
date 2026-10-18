package com.insureease;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableCaching
@EnableScheduling
@OpenAPIDefinition(
        info = @Info(
                title = "InsureEase – InsureGroup Management Platform API",
                version = "1.1.0",
                description = "Multi-tenant SaaS Group Insurance Lifecycle Platform supporting Group Health (GMC), Group Term Life (GTL), and Group Personal Accident (GPA).",
                contact = @Contact(name = "Nitin Chugh", email = "nitin.chugh@insureease.com")
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class InsureEaseApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsureEaseApplication.class, args);
    }
}
