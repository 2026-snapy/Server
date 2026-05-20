package com.gbsw.snapy.domain.blocks.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_blocks")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBlock {

    @EmbeddedId
    private UserBlockId id;

    @CreationTimestamp
    @Column(name = "blocked_at", updatable = false)
    private LocalDateTime blockedAt;
}
