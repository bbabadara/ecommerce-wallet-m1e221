package com.ecommerce.wallet.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import org.springframework.hateoas.RepresentationModel;

@Schema(description = "Détails d'un portefeuille avec liens hypermedia")
public class WalletResponseDTO extends RepresentationModel<WalletResponseDTO> {

    @Schema(description = "Identifiant du client", example = "client123")
    private final String clientId;

    @Schema(description = "Solde actuel en Francs CFA (XOF)", example = "15000")
    private final BigDecimal balance;

    public WalletResponseDTO(String clientId, BigDecimal balance) {
        this.clientId = clientId;
        this.balance = balance;
    }

    public String getClientId() {
        return clientId;
    }

    public BigDecimal getBalance() {
        return balance;
    }

}
