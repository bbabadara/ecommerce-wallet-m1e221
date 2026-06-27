package com.ecommerce.wallet.controller.dto;

import java.math.BigDecimal;
import org.springframework.hateoas.RepresentationModel;

public class WalletResponseDTO extends RepresentationModel<WalletResponseDTO> {

    private final String clientId;
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
