package com.gbsw.snapy.domain.guestbook.entity;

import com.gbsw.snapy.domain.users.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "guest_book")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GuestBook {

    @EmbeddedId
    private GuestBookId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("ownerId")
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("authorId")
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column
    private String image;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
