package com.ecommerce.wallet.controller.dto;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record FundWalletRequestDTO(@Positive BigDecimal amount) {
}
