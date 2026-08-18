package eu.lendo.loancomparisonplatform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI loanComparisonOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Loan Comparison Platform API")
                        .version("1.0.0")
                        .description("""
                                REST API for the Lendo Loan Comparison Platform.

                                The platform allows customers to submit loan applications,
                                receive offers from multiple lenders, compare available offers,
                                and accept the most suitable loan offer.

                                API capabilities include:
                                - Creating and retrieving loan applications
                                - Filtering and paginating loan applications
                                - Submitting lender loan offers
                                - Accepting a loan offer
                                - Managing application and offer lifecycle states
                                """)
                        .contact(new Contact()
                                .name("Engineering Team")));
    }
}