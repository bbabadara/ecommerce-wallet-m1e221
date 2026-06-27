package com.ecommerce.wallet.exception;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Hidden
@ControllerAdvice
public class WalletExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Void> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().build();
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<Void> handleWalletNotFound(WalletNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Void> handleInsufficientBalance(InsufficientBalanceException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }
}
