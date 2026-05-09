package com.sysintegg7.potane.whisperwall.shared.model;

import com.sysintegg7.potane.whisperwall.features.confessions.model.Confession;
import com.sysintegg7.potane.whisperwall.features.comments.model.Comment;
import com.sysintegg7.potane.whisperwall.features.reactions.model.Reaction;
import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Username is required")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Column(nullable = false)
    private String password;

    private String displayName;

    @Column(name = "profile_picture", columnDefinition = "TEXT", length = 1000000)
    private String profilePicture;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(nullable = false)
    private Boolean isVerified = false;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Confession> confessions = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Reaction> reactions = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "blocked_users",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "blocked_user_id")
    )
    private Set<User> blockedUsers = new HashSet<>();

    @Column(nullable = false)
    private Integer reportCount = 0;

    // Restriction fields
    @Column(name = "is_restricted")
    private Boolean isRestricted = false;

    @Column(name = "restriction_end_date")
    private LocalDateTime restrictionEndDate;

    @Column(name = "restriction_reason", columnDefinition = "TEXT")
    private String restrictionReason;

    @Column(name = "restricted_by_admin_id")
    private Long restrictedByAdminId;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
