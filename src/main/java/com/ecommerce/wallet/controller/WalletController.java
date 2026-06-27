package com.ecommerce.wallet.controller;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

import com.ecommerce.wallet.controller.dto.FundWalletRequestDTO;
import com.ecommerce.wallet.controller.dto.WalletResponseDTO;
import com.ecommerce.wallet.service.WalletFundingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Portefeuille Virtuel", description = "Gestion des portefeuilles en Francs CFA (XOF)")
public class WalletController {

    private final WalletFundingService walletFundingService;

    public WalletController(WalletFundingService walletFundingService) {
        this.walletFundingService = walletFundingService;
    }

    @PostMapping("/{clientId}/fund")
    @Operation(summary = "Recharger un portefeuille", description = "Ajoute des fonds au portefeuille du client. Crée automatiquement un nouveau portefeuille si le client n'en possède pas encore.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Portefeuille rechargé avec succès"),
        @ApiResponse(responseCode = "400", description = "Montant invalide (doit être strictement positif)")
    })
    public ResponseEntity<Void> fundWallet(@PathVariable String clientId, @Valid @RequestBody FundWalletRequestDTO request) {
        walletFundingService.addFunds(clientId, request.amount());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{clientId}/pay")
    @Operation(summary = "Effectuer un paiement", description = "Débite le portefeuille du client du montant spécifié.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Paiement effectué avec succès"),
        @ApiResponse(responseCode = "400", description = "Montant invalide (doit être strictement positif)"),
        @ApiResponse(responseCode = "404", description = "Portefeuille introuvable pour ce client"),
        @ApiResponse(responseCode = "409", description = "Solde insuffisant pour effectuer le paiement")
    })
    public ResponseEntity<Void> payFromWallet(@PathVariable String clientId, @Valid @RequestBody FundWalletRequestDTO request) {
        walletFundingService.deductFunds(clientId, request.amount());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{clientId}")
    @Operation(summary = "Consulter le solde", description = "Retourne le solde du portefeuille avec des liens HATEOAS vers les actions possibles.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Détails du portefeuille retournés avec liens hypermedia"),
        @ApiResponse(responseCode = "404", description = "Portefeuille introuvable pour ce client")
    })
    public EntityModel<WalletResponseDTO> getWallet(@PathVariable String clientId) {
        WalletResponseDTO dto = walletFundingService.getBalance(clientId);
        return EntityModel.of(dto,
            linkTo(methodOn(WalletController.class).getWallet(clientId)).withSelfRel(),
            linkTo(methodOn(WalletController.class).fundWallet(clientId, null)).withRel("fund"),
            Link.of("/api/wallets/" + clientId + "/history", "history"));
    }
}
