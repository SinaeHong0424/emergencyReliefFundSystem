package com.ny.safeny.service;

import com.ny.safeny.model.Claim;
import com.ny.safeny.model.Claim.ClaimStatus;
import com.ny.safeny.model.User;
import com.ny.safeny.repository.ClaimRepository;
import com.ny.safeny.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class ClaimService {

    @Autowired
    private ClaimRepository claimRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * 1. Create Claim (User submits claim)
     */
    public Claim createClaim(Claim claim, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        claim.setUser(user);
        
        if (claim.getStatus() == null) {
            claim.setStatus(ClaimStatus.PENDING);
        }
        
        claim.setCreatedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("Creating claim for user: " + username);
        return claimRepository.save(claim);
    }

    /**
     * 2. Get claims by username (User views their own claims)
     */
    public List<Claim> getClaimsByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        return claimRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
    }

    /**
     * 3. Get all claims (Admin views all claims)
     */
    public List<Claim> getAllClaims() {
        return claimRepository.findAll();
    }

    /**
     * 4. Get pending claims (Admin views pending claims)
     */
    public List<Claim> getPendingClaims() {
        return claimRepository.findByStatus(ClaimStatus.PENDING);
    }

    /**
     * 5. Get claim by ID
     */
    public Claim getClaimById(Long id, String username) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // User can only view their own claims, Admin can view all
        if (!claim.getUser().getId().equals(user.getId()) && 
            !user.getRole().name().equals("ROLE_ADMIN")) {
            throw new RuntimeException("Unauthorized access");
        }
        
        return claim;
    }

    /**
     * 6. Update claim (User can only update PENDING claims)
     */
    public Claim updateClaim(Long id, Claim claimUpdate, String username) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only the owner can update
        if (!claim.getUser().getId().equals(user.getId())) {
             throw new RuntimeException("Unauthorized access");
        }

        // Only PENDING claims can be updated
        if (!ClaimStatus.PENDING.equals(claim.getStatus())) {
            throw new RuntimeException("Only pending claims can be updated");
        }

        // Update fields
        if (claimUpdate.getDisasterType() != null) 
            claim.setDisasterType(claimUpdate.getDisasterType());
        if (claimUpdate.getIncidentDate() != null) 
            claim.setIncidentDate(claimUpdate.getIncidentDate());
        if (claimUpdate.getLocation() != null) 
            claim.setLocation(claimUpdate.getLocation());
        if (claimUpdate.getDescription() != null) 
            claim.setDescription(claimUpdate.getDescription());
        if (claimUpdate.getRequestAmount() != null) 
            claim.setRequestAmount(claimUpdate.getRequestAmount());
        
        claim.setUpdatedAt(LocalDateTime.now());
        return claimRepository.save(claim);
    }

    /**
     * 7. Delete claim (User can only delete PENDING claims)
     */
    public void deleteClaim(Long id, String username) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Only the owner can delete
        if (!claim.getUser().getId().equals(user.getId())) {
             throw new RuntimeException("Unauthorized access");
        }
        
        // Only PENDING claims can be deleted
        if (!ClaimStatus.PENDING.equals(claim.getStatus())) {
            throw new RuntimeException("Only pending claims can be deleted");
        }
        
        claimRepository.delete(claim);
    }

    /**
     * 8. Get statistics (Admin)
     */
    public Map<String, Long> getStatistics() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("total", claimRepository.count());
        stats.put("pending", claimRepository.countByStatus(ClaimStatus.PENDING));
        stats.put("underReview", claimRepository.countByStatus(ClaimStatus.UNDER_REVIEW));
        stats.put("approved", claimRepository.countByStatus(ClaimStatus.APPROVED));
        stats.put("rejected", claimRepository.countByStatus(ClaimStatus.REJECTED));
        stats.put("paid", claimRepository.countByStatus(ClaimStatus.PAID));
        return stats;
    }

    /**
     * 9. Approve claim (Admin)
     */
    public Claim approveClaim(Long id, String adminUsername, String reviewComments, BigDecimal approvedAmount) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        // Set approval details
        claim.setStatus(ClaimStatus.APPROVED);
        claim.setReviewerId(admin.getId());
        claim.setReviewComments(reviewComments);
        claim.setApprovedAmount(approvedAmount != null ? approvedAmount : claim.getRequestAmount());
        claim.setReviewedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("Claim " + id + " approved by admin: " + adminUsername);
        return claimRepository.save(claim);
    }

    /**
     * 10. Reject claim (Admin)
     */
    public Claim rejectClaim(Long id, String adminUsername, String reviewComments) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        // Set rejection details
        claim.setStatus(ClaimStatus.REJECTED);
        claim.setReviewerId(admin.getId());
        claim.setReviewComments(reviewComments);
        claim.setReviewedAt(LocalDateTime.now());
        claim.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("Claim " + id + " rejected by admin: " + adminUsername);
        return claimRepository.save(claim);
    }

    /**
     * 11. Update status (Admin - generic status update)
     */
    public Claim updateStatus(Long id, String status) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        claim.setStatus(ClaimStatus.valueOf(status));
        claim.setUpdatedAt(LocalDateTime.now());
        
        return claimRepository.save(claim);
    }

    /**
     * 12. Set claim to UNDER_REVIEW (Admin starts reviewing)
     */
    public Claim setUnderReview(Long id, String adminUsername) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        User admin = userRepository.findByUsername(adminUsername)
                .orElseThrow(() -> new RuntimeException("Admin not found"));
        
        claim.setStatus(ClaimStatus.UNDER_REVIEW);
        claim.setReviewerId(admin.getId());
        claim.setUpdatedAt(LocalDateTime.now());
        
        return claimRepository.save(claim);
    }

    /**
     * 13. Mark claim as PAID (Admin completes payment)
     */
    public Claim markAsPaid(Long id, String adminUsername) {
        Claim claim = claimRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Claim not found"));
        
        if (!ClaimStatus.APPROVED.equals(claim.getStatus())) {
            throw new RuntimeException("Only approved claims can be marked as paid");
        }
        
        claim.setStatus(ClaimStatus.PAID);
        claim.setUpdatedAt(LocalDateTime.now());
        
        System.out.println("Claim " + id + " marked as paid by admin: " + adminUsername);
        return claimRepository.save(claim);
    }
}