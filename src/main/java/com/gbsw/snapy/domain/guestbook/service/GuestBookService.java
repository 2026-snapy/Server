package com.gbsw.snapy.domain.guestbook.service;

import com.gbsw.snapy.domain.blocks.repository.UserBlockRepository;
import com.gbsw.snapy.domain.guestbook.dto.request.GuestBookCreateRequest;
import com.gbsw.snapy.domain.guestbook.dto.response.GuestBookCreateResponse;
import com.gbsw.snapy.domain.guestbook.dto.response.GuestBookResponse;
import com.gbsw.snapy.domain.guestbook.entity.GuestBook;
import com.gbsw.snapy.domain.guestbook.entity.GuestBookId;
import com.gbsw.snapy.domain.guestbook.repository.GuestBookRepository;
import com.gbsw.snapy.domain.notifications.event.GuestbookCreatedEvent;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GuestBookService {

    private final GuestBookRepository guestBookRepository;
    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;
    private final S3Service s3Service;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public GuestBookCreateResponse create(String ownerHandle, GuestBookCreateRequest request, Long authorId) {
        User owner = userRepository.findByHandleAndDeletedAtIsNull(ownerHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (owner.getId().equals(authorId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "본인의 방명록에는 작성할 수 없습니다.");
        }

        // 차단 관계 검사
        if (userBlockRepository.existsBlockBetween(authorId, owner.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        GuestBookId id = new GuestBookId(owner.getId(), authorId);
        if (guestBookRepository.existsById(id)) {
            throw new CustomException(ErrorCode.DUPLICATE_GUEST_BOOK);
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        S3Service.S3UploadResult uploadResult = s3Service.uploadImage(request.getImage(), authorId);

        GuestBook guestBook = GuestBook.builder()
                .id(id)
                .owner(owner)
                .author(author)
                .image(uploadResult.fileUrl())
                .build();

        GuestBook saved = guestBookRepository.saveAndFlush(guestBook);

        eventPublisher.publishEvent(new GuestbookCreatedEvent(owner.getId(), authorId));

        return GuestBookCreateResponse.from(saved);
    }

    // TODO: 추후 MVP 개발이 끝난 후 페이지네이션으로 변경 필요
    @Transactional(readOnly = true)
    public List<GuestBookResponse> getGuestBook(String ownerHandle, Long viewerId) {
        User owner = userRepository.findByHandleAndDeletedAtIsNull(ownerHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 차단 관계일시 방명록 읽기 권한 X
        if (!owner.getId().equals(viewerId)
                && userBlockRepository.existsBlockBetween(viewerId, owner.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        List<GuestBook> guestBooks = guestBookRepository.findByOwnerId(owner.getId());

        // 블락 유저가 작성한 방명록 필터링
        Set<Long> authorIds = guestBooks.stream()
                .map(g -> g.getAuthor().getId())
                .filter(id -> !id.equals(viewerId))
                .collect(Collectors.toSet());
        Set<Long> blockedAuthorIds = authorIds.isEmpty()
                ? Set.of()
                : new HashSet<>(userBlockRepository.findBlockRelatedUserIds(viewerId, authorIds));

        return guestBooks.stream()
                .filter(g -> !blockedAuthorIds.contains(g.getAuthor().getId()))
                .map(GuestBookResponse::from)
                .toList();
    }
}
