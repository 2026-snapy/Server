package com.gbsw.snapy.domain.friend.service;

import com.gbsw.snapy.domain.friend.dto.request.FriendRequestActionRequest.Action;
import com.gbsw.snapy.domain.friend.dto.response.FriendRequestStatusResponse;
import com.gbsw.snapy.domain.friend.dto.response.FriendRequestStatusResponse.Status;
import com.gbsw.snapy.domain.friend.dto.response.FriendResponse;
import com.gbsw.snapy.domain.friend.dto.response.ReceivedFriendRequestResponse;
import com.gbsw.snapy.domain.friend.entity.Friend;
import com.gbsw.snapy.domain.friend.entity.FriendId;
import com.gbsw.snapy.domain.friend.entity.FriendRequest;
import com.gbsw.snapy.domain.friend.repository.FriendRepository;
import com.gbsw.snapy.domain.friend.repository.FriendRequestRepository;
import com.gbsw.snapy.domain.friend.repository.projection.FriendUserProjection;
import com.gbsw.snapy.domain.friend.repository.projection.ReceivedFriendRequestProjection;
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

        if (friendRepository.existsFriendship(senderId, receiver.getId())) {
            throw new CustomException(ErrorCode.ALREADY_FRIEND);
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

    public List<FriendResponse> getFriends(String handle) {
        User user = userRepository.findByHandle(handle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<FriendUserProjection> projections = friendRepository.findFriendsByUserId(user.getId());

        List<FriendResponse> result = new ArrayList<>();
        for (FriendUserProjection p : projections) {
            result.add(FriendResponse.from(p));
        }

        return result;
    }

    public List<ReceivedFriendRequestResponse> getReceivedRequests(Long userId) {
        List<ReceivedFriendRequestProjection> projections = friendRequestRepository.findReceivedRequests(userId);

        List<ReceivedFriendRequestResponse> result = new ArrayList<>();
        for (ReceivedFriendRequestProjection p : projections) {
            result.add(new ReceivedFriendRequestResponse(p.getRequestId(), p.getHandle(), p.getUsername(), p.getProfileImageUrl()));
        }

        return result;
    }

    @Transactional
    public void processRequest(Long receiverId, Long requestId, Action action) {
        FriendRequest request = friendRequestRepository.findById(requestId)
                .orElseThrow(() -> new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND));

        if (!request.getReceiverId().equals(receiverId)) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_ACCESS_DENIED);
        }

        if (action == Action.APPROVE) {
            Long userAId = Math.min(request.getSenderId(), request.getReceiverId());
            Long userBId = Math.max(request.getSenderId(), request.getReceiverId());
            friendRepository.save(Friend.builder().id(new FriendId(userAId, userBId)).build());
        }

        friendRequestRepository.delete(request);
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
