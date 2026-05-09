package com.sysintegg7.potane.whisperwall.features.restrictions.model;

import com.sysintegg7.potane.whisperwall.shared.model.User;
import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "restriction_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RestrictionRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_to_restrict_id", nullable = false)
    private User userToRestrict;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requested_by_moderator_id", nullable = false)
    private User requestedByModerator;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(nullable = false)
    private Integer requestedDurationDays = 7; // Default 7 days

    @Column(nullable = false)
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_admin_id")
    private User reviewedByAdmin;

    @Column(columnDefinition = "TEXT")
    private String adminResponse;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    private LocalDateTime reviewedAt;
}
