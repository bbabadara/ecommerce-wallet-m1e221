package com.ecommerce.wallet.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Portefeuille Virtuel")
                .version("1.0.0")
                .description("API de gestion de portefeuilles virtuels en Francs CFA (XOF) pour la plateforme E-commerce.\n\n"
                    + "Cette API permet de :\n"
                    + "- Recharger un portefeuille (avec création automatique)\n"
                    + "- Effectuer des paiements\n"
                    + "- Consulter le solde avec navigation HATEOAS\n\n"
                    + "Niveaux de maturité REST implémentés :\n"
                    + "- **Niveau 2** : Ressources + Verbes HTTP standards\n"
                    + "- **Niveau 3** : HATEOAS (Hypermedia)")
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0")));
    }
}
