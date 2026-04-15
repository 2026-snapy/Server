package com.gbsw.snapy.domain.comments.repository;

import com.gbsw.snapy.domain.comments.entity.CommentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentAttachmentRepository extends JpaRepository<CommentAttachment, Long> {
}
