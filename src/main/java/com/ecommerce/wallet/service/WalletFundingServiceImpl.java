package com.ecommerce.wallet.service;

import com.ecommerce.wallet.controller.dto.WalletResponseDTO;
import com.ecommerce.wallet.entity.Wallet;
import com.ecommerce.wallet.event.LowBalanceEvent;
import com.ecommerce.wallet.exception.WalletNotFoundException;
import com.ecommerce.wallet.repository.WalletRepository;
import java.math.BigDecimal;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class WalletFundingServiceImpl implements WalletFundingService {

    private static final BigDecimal LOW_BALANCE_THRESHOLD = new BigDecimal("5000");

    private final WalletRepository walletRepository;
    private final ApplicationEventPublisher eventPublisher;

    WalletFundingServiceImpl(WalletRepository walletRepository, ApplicationEventPublisher eventPublisher) {
        this.walletRepository = walletRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void addFunds(String clientId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByClientId(clientId)
            .orElseGet(() -> new Wallet(clientId, BigDecimal.ZERO));
        wallet.addFunds(amount);
        walletRepository.save(wallet);
    }

    @Override
    @Transactional
    public void deductFunds(String clientId, BigDecimal amount) {
        Wallet wallet = walletRepository.findByClientId(clientId)
            .orElseThrow(() -> new WalletNotFoundException(clientId));
        wallet.deductFunds(amount);
        walletRepository.save(wallet);

        if (wallet.getBalance().compareTo(LOW_BALANCE_THRESHOLD) < 0) {
            eventPublisher.publishEvent(new LowBalanceEvent(clientId, wallet.getBalance()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WalletResponseDTO getBalance(String clientId) {
        Wallet wallet = walletRepository.findByClientId(clientId)
            .orElseThrow(() -> new WalletNotFoundException(clientId));
        return new WalletResponseDTO(wallet.getClientId(), wallet.getBalance());
    }
}
