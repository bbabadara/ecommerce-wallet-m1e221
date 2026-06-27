package com.ecommerce.wallet.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

@Schema(description = "Requête de recharge ou de paiement d'un portefeuille")
public record FundWalletRequestDTO(
    @Schema(description = "Montant en Francs CFA (XOF)", example = "5000", minimum = "1")
    @Positive
    BigDecimal amount
) {
}
