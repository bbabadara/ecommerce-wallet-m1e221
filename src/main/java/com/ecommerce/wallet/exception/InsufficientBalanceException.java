package com.ecommerce.wallet.exception;

import java.math.BigDecimal;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String clientId, BigDecimal balance, BigDecimal amount) {
        super("Insufficient balance for client " + clientId + ": balance=" + balance + ", required=" + amount);
    }
}
