package com.gbsw.snapy.domain.guestbook.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GuestBookId implements Serializable {

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(name = "author_id", nullable = false)
    private Long authorId;
}
