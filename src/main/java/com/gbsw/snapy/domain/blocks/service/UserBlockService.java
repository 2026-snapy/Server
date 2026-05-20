package com.gbsw.snapy.domain.blocks.service;

import com.gbsw.snapy.domain.blocks.dto.response.BlockedUserResponse;
import com.gbsw.snapy.domain.blocks.entity.UserBlock;
import com.gbsw.snapy.domain.blocks.entity.UserBlockId;
import com.gbsw.snapy.domain.blocks.repository.UserBlockRepository;
import com.gbsw.snapy.domain.blocks.repository.projection.BlockedUserProjection;
import com.gbsw.snapy.domain.friends.repository.FriendRepository;
import com.gbsw.snapy.domain.friends.repository.FriendRequestRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final FriendRequestRepository friendRequestRepository;

    @Transactional
    public void blockUser(Long myId, String targetHandle) {
        User target = userRepository.findByHandleAndDeletedAtIsNull(targetHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (myId.equals(target.getId())) {
            throw new CustomException(ErrorCode.USER_BLOCK_SELF);
        }

        if (userBlockRepository.existsById_UserIdAndId_TargetUserId(myId, target.getId())) {
            throw new CustomException(ErrorCode.ALREADY_BLOCKED);
        }

        // 차단 시 기존 친구관계와 양방향 친구 신청을 모두 제거
        if (friendRepository.existsFriendship(myId, target.getId())) {
            friendRepository.deleteFriendship(myId, target.getId());
        }
        friendRequestRepository.deleteBySenderIdAndReceiverId(myId, target.getId());
        friendRequestRepository.deleteBySenderIdAndReceiverId(target.getId(), myId);

        userBlockRepository.save(UserBlock.builder()
                .id(new UserBlockId(myId, target.getId()))
                .build());
    }

    @Transactional
    public void unblockUser(Long myId, String targetHandle) {
        User target = userRepository.findByHandleAndDeletedAtIsNull(targetHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!userBlockRepository.existsById_UserIdAndId_TargetUserId(myId, target.getId())) {
            throw new CustomException(ErrorCode.USER_BLOCK_NOT_FOUND);
        }

        userBlockRepository.deleteById_UserIdAndId_TargetUserId(myId, target.getId());
    }

    public List<BlockedUserResponse> getBlockedUsers(Long myId) {
        List<BlockedUserProjection> projections = userBlockRepository.findBlockedUsersByUserId(myId);

        List<BlockedUserResponse> result = new ArrayList<>();
        for (BlockedUserProjection p : projections) {
            result.add(BlockedUserResponse.from(p));
        }
        return result;
    }
}
