package com.group2.backend.controller;

import com.group2.backend.dto.AccountCreateDto;
import com.group2.backend.dto.AccountDto;
import com.group2.backend.dto.AccountSummaryDto;
import com.group2.backend.dto.AccountUpdateDto;
import com.group2.backend.dto.ArtworkSummaryDto;
import com.group2.backend.service.AccountService;
import com.group2.backend.service.ArtworkService;
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
@RequestMapping("/accounts")
@RequiredArgsConstructor
@Tag(name = "accounts", description = "Account management endpoints")
public class AccountController {

    private final AccountService accountService;
    private final ArtworkService artworkService;

    @GetMapping
    @Operation(summary = "List accounts", description = "Returns account summaries")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK")
    })
    public ResponseEntity<List<AccountSummaryDto>> list() {
        return ResponseEntity.ok(accountService.getAccounts());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account", description = "Get account by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<AccountDto> getById(@PathVariable String id) {
        return ResponseEntity.ok(accountService.getAccount(id));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current account", description = "Returns the authenticated account.", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AccountDto> me() {
        return ResponseEntity.ok(accountService.getCurrentAccount());
    }

    @PostMapping
    @Operation(summary = "Create account", description = "Create a new account")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    public ResponseEntity<AccountDto> create(@RequestBody AccountCreateDto body) {
        AccountDto created = accountService.createAccount(body);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/me")
    @Operation(summary = "Update current account", description = "Update authenticated user's profile", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AccountDto> updateCurrent(@RequestBody AccountUpdateDto body) {
        return ResponseEntity.ok(accountService.updateCurrentAccount(body));
    }

    @GetMapping("/{id}/artworks")
    @Operation(summary = "List account artworks", description = "List artworks created by account")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "404", description = "Not found")
    })
    public ResponseEntity<List<ArtworkSummaryDto>> listAccountArtworks(@PathVariable String id) {
        accountService.getAccount(id); // ensure existence or 404
        return ResponseEntity.ok(artworkService.listByAccount(id));
    }

}
