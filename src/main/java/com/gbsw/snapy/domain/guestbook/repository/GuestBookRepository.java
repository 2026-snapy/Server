package com.gbsw.snapy.domain.guestbook.repository;

import com.gbsw.snapy.domain.guestbook.entity.GuestBook;
import com.gbsw.snapy.domain.guestbook.entity.GuestBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GuestBookRepository extends JpaRepository<GuestBook, GuestBookId> {
}
