package com.gbsw.snapy.domain.friend.service;

import com.gbsw.snapy.domain.friend.dto.response.FriendRequestStatusResponse;
import com.gbsw.snapy.domain.friend.dto.response.FriendRequestStatusResponse.Status;
import com.gbsw.snapy.domain.friend.entity.FriendRequest;
import com.gbsw.snapy.domain.friend.repository.FriendRepository;
import com.gbsw.snapy.domain.friend.repository.FriendRequestRepository;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final UserRepository userRepository;

    public void sendRequest(Long senderId, String receiverHandle) {
        User receiver = userRepository.findByHandle(receiverHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (senderId.equals(receiver.getId())) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_SELF);
        }

        if (friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiver.getId())) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_ALREADY_SENT);
        }

        friendRequestRepository.save(FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiver.getId())
                .build());
    }

    public FriendRequestStatusResponse getRequestStatus(Long senderId, String receiverHandle) {
        User receiver = userRepository.findByHandle(receiverHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (friendRepository.existsFriendship(senderId, receiver.getId())) {
            return new FriendRequestStatusResponse(Status.FRIEND);
        }

        if (friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiver.getId())) {
            return new FriendRequestStatusResponse(Status.PENDING);
        }

        if (friendRequestRepository.existsBySenderIdAndReceiverId(receiver.getId(), senderId)) {
            return new FriendRequestStatusResponse(Status.RECEIVED);
        }

        return new FriendRequestStatusResponse(Status.NONE);
    }

    @Transactional
    public void cancelRequest(Long senderId, String receiverHandle) {
        User receiver = userRepository.findByHandle(receiverHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiver.getId())) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }

        friendRequestRepository.deleteBySenderIdAndReceiverId(senderId, receiver.getId());
    }
}
