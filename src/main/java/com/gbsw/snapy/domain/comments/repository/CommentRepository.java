package com.gbsw.snapy.domain.comments.repository;

import com.gbsw.snapy.domain.comments.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
}
