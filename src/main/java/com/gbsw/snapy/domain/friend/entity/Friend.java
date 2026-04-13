package com.gbsw.snapy.domain.friend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

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

    @CreatedDate
    private LocalDateTime createdAt;
}
