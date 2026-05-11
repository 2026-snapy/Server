package com.gbsw.snapy.domain.users.service;

import com.gbsw.snapy.domain.users.dto.request.UpdateHandleRequest;
import com.gbsw.snapy.domain.users.dto.request.UpdatePhoneRequest;
import com.gbsw.snapy.domain.users.dto.request.UpdateUsernameRequest;
import com.gbsw.snapy.domain.users.dto.response.CheckHandleResponse;
import com.gbsw.snapy.domain.users.dto.response.UpdateBackgroundImageResponse;
import com.gbsw.snapy.domain.users.dto.response.UpdateProfileImageResponse;
import com.gbsw.snapy.domain.users.dto.response.UserProfileResponse;
import com.gbsw.snapy.domain.users.dto.response.UserSearchResponse;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.auth.repository.RefreshTokenRepository;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.domain.friends.repository.FriendRepository;
import com.gbsw.snapy.domain.streaks.service.StreakService;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.infra.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final S3Service s3Service;
    private final StreakService streakService;

    public UserProfileResponse getProfile(String handle) {
        User user = userRepository.findByHandleAndDeletedAtIsNull(handle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        long friendCount = friendRepository.countFriendsByUserId(user.getId());
        StreakService.StreakSummary streak = streakService.getSummary(user.getId());
        return UserProfileResponse.from(user, friendCount, streak.current(), streak.max());
    }

    public UserProfileResponse getMyProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        long friendCount = friendRepository.countFriendsByUserId(userId);
        StreakService.StreakSummary streak = streakService.getSummary(userId);
        return UserProfileResponse.from(user, friendCount, streak.current(), streak.max());
    }

    public UpdateBackgroundImageResponse updateBackgroundImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.IMAGE_EMPTY);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String oldKey = user.getBackGroundImageKey();

        S3Service.S3UploadResult result = s3Service.uploadImage(file, userId);

        try {
            user.setBackGroundImageUrl(result.fileUrl());
            user.setBackGroundImageKey(result.s3Key());
        } catch (Exception e) {
            s3Service.delete(result.s3Key());
            throw e;
        }

        if (oldKey != null) s3Service.delete(oldKey);
        userRepository.save(user);

        return UpdateBackgroundImageResponse.from(user);
    }

    public UpdateProfileImageResponse updateProfileImage(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.IMAGE_EMPTY);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        String oldKey = user.getProfileImageKey();

        S3Service.S3UploadResult result = s3Service.uploadImage(file, userId);

        try {
            user.setProfileImageUrl(result.fileUrl());
            user.setProfileImageKey(result.s3Key());
        } catch (Exception e) {
            s3Service.delete(result.s3Key());
            throw e;
        }

        if (oldKey != null) s3Service.delete(oldKey);
        userRepository.save(user);

        return UpdateProfileImageResponse.from(user);
    }

    @org.springframework.transaction.annotation.Transactional
    public void updatePhone(Long userId, UpdatePhoneRequest dto) {
        if (userRepository.existsByPhone(dto.getPhone())) {
            throw new CustomException(ErrorCode.DUPLICATE_PHONE);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setPhone(dto.getPhone());
    }

    public List<UserSearchResponse> searchUsers(String q) {
        List<User> users = userRepository.searchActiveUsers(q);

        List<UserSearchResponse> result = new ArrayList<>();
        for (User user : users) {
            result.add(UserSearchResponse.from(user));
        }

        return result;
    }

    @Transactional
    public void deleteAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        refreshTokenRepository.deleteAllByUserId(userId);
        user.delete();
    }

    public CheckHandleResponse checkHandle(String handle) {
        boolean available = !userRepository.existsByHandle(handle);
        return new CheckHandleResponse(available);
    }

    @Transactional
    public void updateHandle(Long userId, UpdateHandleRequest request) {
        if (userRepository.existsByHandle(request.getHandle())) {
            throw new CustomException(ErrorCode.DUPLICATE_HANDLE);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        user.setHandle(request.getHandle());
    }

    @Transactional
    public void updateUsername(Long userId, UpdateUsernameRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        user.setUsername(request.getUsername());
    }
}
