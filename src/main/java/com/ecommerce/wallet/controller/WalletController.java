package com.ecommerce.wallet.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecommerce.wallet.controller.dto.FundWalletRequestDTO;
import com.ecommerce.wallet.controller.dto.WalletResponseDTO;
import com.ecommerce.wallet.service.WalletFundingService;
import jakarta.validation.Valid;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletFundingService walletFundingService;

    public WalletController(WalletFundingService walletFundingService) {
        this.walletFundingService = walletFundingService;
    }

    @PostMapping("/{clientId}/fund")
    public ResponseEntity<Void> fundWallet(@PathVariable String clientId, @Valid @RequestBody FundWalletRequestDTO request) {
        walletFundingService.addFunds(clientId, request.amount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clientId}/pay")
    public ResponseEntity<Void> payFromWallet(@PathVariable String clientId, @Valid @RequestBody FundWalletRequestDTO request) {
        walletFundingService.deductFunds(clientId, request.amount());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{clientId}")
    public EntityModel<WalletResponseDTO> getWallet(@PathVariable String clientId) {
        WalletResponseDTO dto = walletFundingService.getBalance(clientId);
        return EntityModel.of(dto,
            linkTo(methodOn(WalletController.class).getWallet(clientId)).withSelfRel(),
            linkTo(methodOn(WalletController.class).fundWallet(clientId, null)).withRel("fund"),
            Link.of("/api/wallets/" + clientId + "/history", "history"));
    }
}
