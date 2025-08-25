package com.ny.safeny.controller;

import com.ny.safeny.model.Claim;
import com.ny.safeny.service.ClaimService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/claims")
@CrossOrigin(origins = "http://localhost:4200", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class ClaimController {

    @Autowired
    private ClaimService claimService;

    // 1. Submit Claim (User)
@PostMapping
@PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
public ResponseEntity<Claim> createClaim(@Valid @RequestBody Claim claim, Authentication authentication) {
    try {
        System.out.println("=== Create Claim Request ===");
        System.out.println("User: " + authentication.getName());
        System.out.println("Disaster Type: " + claim.getDisasterType());
        System.out.println("Incident Date: " + claim.getIncidentDate());
        System.out.println("Location: " + claim.getLocation());
        System.out.println("Description: " + claim.getDescription());
        System.out.println("Request Amount: " + claim.getRequestAmount());
        
        String username = authentication.getName();
        Claim createdClaim = claimService.createClaim(claim, username);
        
        System.out.println("=== Claim Created Successfully ===");
        System.out.println("Claim ID: " + createdClaim.getId());
        
        return new ResponseEntity<>(createdClaim, HttpStatus.CREATED);
    } catch (Exception e) {
        System.err.println("Error creating claim: " + e.getMessage());
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
}

    // 2. Get My Claims (User)
    @GetMapping("/my-claims")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<List<Claim>> getMyClaims(Authentication authentication) {
        try {
            String username = authentication.getName();
            List<Claim> claims = claimService.getClaimsByUsername(username);
            return ResponseEntity.ok(claims);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 3. Get All Claims (Admin)
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Claim>> getAllClaims() {
        try {
            List<Claim> claims = claimService.getAllClaims();
            return ResponseEntity.ok(claims);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 4. Get Pending Claims (Admin)
    @GetMapping("/pending")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Claim>> getPendingClaims() {
        try {
            List<Claim> pendingClaims = claimService.getPendingClaims();
            return ResponseEntity.ok(pendingClaims);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 5. Get Claim by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Claim> getClaimById(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            Claim claim = claimService.getClaimById(id, username);
            return ResponseEntity.ok(claim);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    // 6. Update Claim (User - only PENDING claims)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Claim> updateClaim(@PathVariable Long id, @Valid @RequestBody Claim claim, Authentication authentication) {
        try {
            String username = authentication.getName();
            Claim updatedClaim = claimService.updateClaim(id, claim, username);
            return ResponseEntity.ok(updatedClaim);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 7. Delete Claim (User - only PENDING claims)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClaim(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            claimService.deleteClaim(id, username);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 8. Get Statistics (Admin)
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getStatistics() {
        try {
            Map<String, Long> stats = claimService.getStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 9. Approve Claim (Admin)
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Claim> approveClaim(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            Authentication authentication) {
        try {
            String adminUsername = authentication.getName();
            String reviewComments = (String) body.get("reviewComments");
            BigDecimal approvedAmount = body.get("approvedAmount") != null 
                ? new BigDecimal(body.get("approvedAmount").toString()) 
                : null;
            
            Claim approvedClaim = claimService.approveClaim(id, adminUsername, reviewComments, approvedAmount);
            return ResponseEntity.ok(approvedClaim);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 10. Reject Claim (Admin)
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Claim> rejectClaim(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            String adminUsername = authentication.getName();
            String reviewComments = body.get("reviewComments");
            
            Claim rejectedClaim = claimService.rejectClaim(id, adminUsername, reviewComments);
            return ResponseEntity.ok(rejectedClaim);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 11. Update Status (Admin - generic status update)
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Claim> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            String status = body.get("status");
            Claim updatedClaim = claimService.updateStatus(id, status);
            return ResponseEntity.ok(updatedClaim);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}