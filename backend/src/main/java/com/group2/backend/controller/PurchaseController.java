package com.group2.backend.controller;

import com.group2.backend.dto.PurchaseDto;
import com.group2.backend.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/purchases")
@RequiredArgsConstructor
@Tag(name = "purchases", description = "Purchase endpoints")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping("/{artworkId}")
    @Operation(summary = "Create purchase", description = "Purchase an artwork", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Created"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<PurchaseDto> purchase(@PathVariable String artworkId) {
        PurchaseDto created = purchaseService.createPurchase(artworkId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    @Operation(summary = "My purchases", description = "List purchases for current user", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "OK"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<List<PurchaseDto>> myPurchases() {
        return ResponseEntity.ok(purchaseService.getMyPurchases());
    }
}
