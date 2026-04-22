package com.gbsw.snapy.domain.guestbook.service;

import com.gbsw.snapy.domain.guestbook.dto.request.GuestBookCreateRequest;
import com.gbsw.snapy.domain.guestbook.dto.response.GuestBookCreateResponse;
import com.gbsw.snapy.domain.guestbook.dto.response.GuestBookResponse;
import com.gbsw.snapy.domain.guestbook.entity.GuestBook;
import com.gbsw.snapy.domain.guestbook.entity.GuestBookId;
import com.gbsw.snapy.domain.guestbook.repository.GuestBookRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestBookService {

    private final GuestBookRepository guestBookRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Transactional
    public GuestBookCreateResponse create(String ownerHandle, GuestBookCreateRequest request, Long authorId) {
        User owner = userRepository.findByHandle(ownerHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (owner.getId().equals(authorId)) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE, "본인의 방명록에는 작성할 수 없습니다.");
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

        return GuestBookCreateResponse.from(saved);
    }

    // TODO: 추후 MVP 개발이 끝난 후 페이지네이션으로 변경 필요
    @Transactional(readOnly = true)
    public List<GuestBookResponse> getGuestBook(String ownerHandle) {
        User owner = userRepository.findByHandle(ownerHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return guestBookRepository.findByOwnerId(owner.getId()).stream()
                .map(GuestBookResponse::from)
                .toList();
    }
}
