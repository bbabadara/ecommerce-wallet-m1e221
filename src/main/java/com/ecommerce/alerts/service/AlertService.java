package com.ecommerce.alerts.service;

import com.ecommerce.alerts.entity.Alert;
import com.ecommerce.alerts.repository.AlertRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    @Transactional
    public void createAlert(String clientId, String message) {
        Alert alert = new Alert(clientId, message);
        alertRepository.save(alert);
    }
}
