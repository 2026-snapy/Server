package com.gbsw.snapy.domain.guestbook.repository;

import com.gbsw.snapy.domain.guestbook.entity.GuestBook;
import com.gbsw.snapy.domain.guestbook.entity.GuestBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GuestBookRepository extends JpaRepository<GuestBook, GuestBookId> {

    @Query("SELECT g FROM GuestBook g JOIN FETCH g.author " +
            "WHERE g.owner.id = :ownerId " +
            "ORDER BY g.createdAt DESC")
    List<GuestBook> findByOwnerId(@Param("ownerId") Long ownerId);
}
