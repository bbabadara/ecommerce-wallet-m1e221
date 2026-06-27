package com.ecommerce.wallet.service;

import com.ecommerce.wallet.controller.dto.WalletResponseDTO;
import java.math.BigDecimal;

public interface WalletFundingService {

    void addFunds(String clientId, BigDecimal amount);

    void deductFunds(String clientId, BigDecimal amount);

    WalletResponseDTO getBalance(String clientId);
}
