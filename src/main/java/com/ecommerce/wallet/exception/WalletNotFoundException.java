package com.ecommerce.wallet.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException(String clientId) {
        super("Wallet not found for client: " + clientId);
    }
}
