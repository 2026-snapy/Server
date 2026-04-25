package com.gbsw.snapy.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Common
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 입력값입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),

    // JWT
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "만료된 토큰입니다."),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "지원하지 않는 토큰입니다."),
    EMPTY_TOKEN(HttpStatus.UNAUTHORIZED, "토큰이 비어있습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 리프레시 토큰입니다."),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 올바르지 않습니다."),
    DUPLICATE_HANDLE(HttpStatus.CONFLICT, "이미 사용 중인 핸들입니다."),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다."),
    DUPLICATE_PHONE(HttpStatus.CONFLICT, "이미 사용 중인 전화번호입니다."),

    // S3 general
    FILE_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 삭제에 실패했습니다."),

    // Image
    IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "이미지가 비어있습니다."),
    INVALID_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다."),
    IMAGE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "이미지 크기가 10MB를 초과합니다."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패했습니다."),
    IMAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "이미지를 찾을 수 없습니다."),

    // Audio
    AUDIO_EMPTY(HttpStatus.BAD_REQUEST, "오디오 파일이 비어있습니다."),
    INVALID_AUDIO_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 오디오 파일 형식입니다. (.m4a만 허용)"),
    AUDIO_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "오디오 파일 크기가 20MB를 초과합니다."),
    AUDIO_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "오디오 업로드에 실패했습니다."),
    AUDIO_NOT_FOUND(HttpStatus.NOT_FOUND, "오디오를 찾을 수 없습니다."),

    // Album
    ALBUM_NOT_FOUND(HttpStatus.NOT_FOUND, "앨범을 찾을 수 없습니다."),
    ALBUM_PHOTO_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "앨범 사진 개수를 초과했습니다."),
    DUPLICATE_ALBUM_PHOTO_TYPE(HttpStatus.CONFLICT, "해당 시간대에 이미 사진이 등록되어 있습니다."),
    INVALID_ALBUM_PHOTO_TIME_SLOT(HttpStatus.BAD_REQUEST, "현재 시간대에 업로드할 수 없는 사진 타입입니다."),
    INVALID_MONTH(HttpStatus.BAD_REQUEST, "유효하지 않은 월입니다. (1~12)"),
    ALBUM_DAILY_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "하루 업로드 한도(5장)를 초과했습니다."),
    ALBUM_ALREADY_PUBLISHED(HttpStatus.CONFLICT, "이미 게시된 앨범입니다."),

    // Story
    STORY_NOT_FOUND(HttpStatus.NOT_FOUND, "스토리를 찾을 수 없습니다."),
    STORY_EXPIRED(HttpStatus.BAD_REQUEST, "만료된 스토리입니다."),
    STORY_PHOTO_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 시간대의 스토리 사진을 찾을 수 없습니다."),
    CANNOT_LIKE_OWN_STORY(HttpStatus.BAD_REQUEST, "본인의 스토리에는 좋아요를 누를 수 없습니다."),

    // Notification
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다."),

    // FriendRequest
    ALREADY_FRIEND(HttpStatus.CONFLICT, "이미 친구 관계입니다."),
    FRIEND_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 관계를 찾을 수 없습니다."),
    FRIEND_REQUEST_ALREADY_SENT(HttpStatus.CONFLICT, "이미 친구 신청을 보냈습니다."),
    FRIEND_REQUEST_SELF(HttpStatus.BAD_REQUEST, "자기 자신에게 친구 신청을 할 수 없습니다."),
    FRIEND_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "친구 신청을 찾을 수 없습니다."),
    FRIEND_REQUEST_ACCESS_DENIED(HttpStatus.FORBIDDEN, "해당 친구 신청에 대한 권한이 없습니다."),

    // Comment
    COMMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "댓글을 찾을 수 없습니다."),
    INVALID_COMMENT_ATTACHMENT(HttpStatus.BAD_REQUEST, "댓글 첨부 데이터가 올바르지 않습니다."),

    // GuestBook
    GUEST_BOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "방명록을 찾을 수 없습니다."),
    DUPLICATE_GUEST_BOOK(HttpStatus.CONFLICT, "이미 방명록을 작성했습니다."),

    // OAuth
    GOOGLE_LOGIN_FAILED(HttpStatus.UNAUTHORIZED, "Google 로그인에 실패했습니다.");

    private final HttpStatus status;
    private final String message;
}
