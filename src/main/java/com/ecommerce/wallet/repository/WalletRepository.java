package com.ecommerce.wallet.repository;

import com.ecommerce.wallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {

    Optional<Wallet> findByClientId(String clientId);
}
