package com.ecommerce.alerts.repository;

import com.ecommerce.alerts.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, Long> {
}
