package com.sysintegg7.potane.whisperwall.model;

import javax.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "reactions",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"confession_id", "user_id"})
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reactionType; // heart, hug, support, condolences, etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "confession_id")
    private Confession confession;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
