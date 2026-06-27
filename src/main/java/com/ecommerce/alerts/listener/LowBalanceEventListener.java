package com.ecommerce.alerts.listener;

import com.ecommerce.alerts.service.AlertService;
import com.ecommerce.wallet.event.LowBalanceEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class LowBalanceEventListener {

    private final AlertService alertService;

    public LowBalanceEventListener(AlertService alertService) {
        this.alertService = alertService;
    }

    @EventListener
    public void handleLowBalance(LowBalanceEvent event) {
        String message = "Low balance alert for client " + event.getClientId()
            + ": current balance = " + event.getCurrentBalance() + " XOF";
        alertService.createAlert(event.getClientId(), message);
    }
}
