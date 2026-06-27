package com.ecommerce.wallet.event;

import java.math.BigDecimal;

public class LowBalanceEvent {

    private final String clientId;
    private final BigDecimal currentBalance;

    public LowBalanceEvent(String clientId, BigDecimal currentBalance) {
        this.clientId = clientId;
        this.currentBalance = currentBalance;
    }

    public String getClientId() {
        return clientId;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }
}
