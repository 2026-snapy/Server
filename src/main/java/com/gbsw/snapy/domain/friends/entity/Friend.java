package com.gbsw.snapy.domain.friends.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "friends")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Friend {

    @EmbeddedId
    private FriendId id;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
