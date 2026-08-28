package com.group2.backend.controller;

import com.group2.backend.dto.AccountDto;
import com.group2.backend.dto.LoginDto;
import com.group2.backend.model.Account;
import com.group2.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "auth", description = "Authentication endpoints")
public class AuthController {

    private final AuthService authService;

    @GetMapping("/status")
    @Operation(summary = "Authentication status", description = "Returns the authenticated account.", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Authenticated", content = @Content(schema = @Schema(implementation = AccountDto.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<AccountDto> status() {
        Account account = authService.getAccountFromRequest();
        return ResponseEntity.ok(account.toDto());
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Public. Logs in with identifier and password and sets the session cookie.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logged in"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<Void> login(@RequestBody LoginDto body,
                                      @RequestParam(name = "remember", defaultValue = "false") boolean remember) {
        authService.loginAndSetCookie(body.getIdentifier(), body.getPassword(), remember);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Authenticated users only. Optionally revoke all tokens.", security = @SecurityRequirement(name = "bearer-jwt"))
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Logged out"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> logout(@RequestParam(defaultValue = "false") boolean hard) {
        authService.logout(hard);
        return ResponseEntity.ok().build();
    }
}
