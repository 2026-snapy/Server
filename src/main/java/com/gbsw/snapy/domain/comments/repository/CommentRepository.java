package com.gbsw.snapy.domain.comments.repository;

import com.gbsw.snapy.domain.comments.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user " +
            "LEFT JOIN FETCH c.attachment a " +
            "LEFT JOIN FETCH a.photo " +
            "LEFT JOIN FETCH a.audio " +
            "WHERE c.album.id = :albumId AND c.id < :cursor " +
            "ORDER BY c.id DESC " +
            "LIMIT :size")
    List<Comment> findByAlbumIdWithCursor(
            @Param("albumId") Long albumId,
            @Param("cursor") Long cursor,
            @Param("size") int size
    );

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.user " +
            "LEFT JOIN FETCH c.attachment a " +
            "LEFT JOIN FETCH a.photo " +
            "LEFT JOIN FETCH a.audio " +
            "WHERE c.album.id = :albumId " +
            "ORDER BY c.id DESC " +
            "LIMIT :size")
    List<Comment> findByAlbumIdLatest(
            @Param("albumId") Long albumId,
            @Param("size") int size
    );
}
