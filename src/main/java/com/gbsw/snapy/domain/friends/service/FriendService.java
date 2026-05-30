package com.gbsw.snapy.domain.friends.service;

import com.gbsw.snapy.domain.friends.dto.request.FriendRequestActionRequest.Action;
import com.gbsw.snapy.domain.friends.dto.response.FriendRequestStatusResponse;
import com.gbsw.snapy.domain.friends.dto.response.FriendRequestStatusResponse.Status;
import com.gbsw.snapy.domain.friends.dto.response.FriendResponse;
import com.gbsw.snapy.domain.friends.dto.response.ReceivedFriendRequestResponse;
import com.gbsw.snapy.domain.friends.entity.Friend;
import com.gbsw.snapy.domain.friends.entity.FriendId;
import com.gbsw.snapy.domain.friends.entity.FriendRequest;
import com.gbsw.snapy.domain.blocks.repository.UserBlockRepository;
import com.gbsw.snapy.domain.friends.repository.FriendRepository;
import com.gbsw.snapy.domain.friends.repository.FriendRequestRepository;
import com.gbsw.snapy.domain.friends.repository.projection.FriendUserProjection;
import com.gbsw.snapy.domain.friends.repository.projection.ReceivedFriendRequestProjection;
import com.gbsw.snapy.domain.friends.repository.projection.RecommendedFriendProjection;
import com.gbsw.snapy.domain.users.entity.User;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.domain.notifications.event.FriendAcceptedEvent;
import com.gbsw.snapy.domain.notifications.event.FriendRequestEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FriendService {

    private static final int RECOMMENDATION_LIMIT = 50;

    private final FriendRequestRepository friendRequestRepository;
    private final FriendRepository friendRepository;
    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void sendRequest(Long senderId, String receiverHandle) {
        User receiver = userRepository.findByHandleAndDeletedAtIsNull(receiverHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (senderId.equals(receiver.getId())) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_SELF);
        }

        if (userBlockRepository.existsBlockBetween(senderId, receiver.getId())) {
            throw new CustomException(ErrorCode.ACCESS_DENIED);
        }

        if (friendRepository.existsFriendship(senderId, receiver.getId())) {
            throw new CustomException(ErrorCode.ALREADY_FRIEND);
        }

        if (friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiver.getId())) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_ALREADY_SENT);
        }

        FriendRequest savedRequest = friendRequestRepository.save(FriendRequest.builder()
                .senderId(senderId)
                .receiverId(receiver.getId())
                .build());

        eventPublisher.publishEvent(new FriendRequestEvent(
                savedRequest.getId(), senderId, receiver.getId()));
    }

    public FriendRequestStatusResponse getRequestStatus(Long senderId, String receiverHandle) {
        User receiver = userRepository.findByHandleAndDeletedAtIsNull(receiverHandle)
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
        User user = userRepository.findByHandleAndDeletedAtIsNull(handle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<FriendUserProjection> projections = friendRepository.findFriendsByUserId(user.getId());

        List<FriendResponse> result = new ArrayList<>();
        for (FriendUserProjection p : projections) {
            result.add(FriendResponse.from(p));
        }

        return result;
    }

    public List<FriendResponse> getRecommendedFriends(Long myId) {
        // 친구 추가 하지 않은 겹지인을 반환합니다
        // 겹지인이 많을 수록 먼저 노출되고, 동일한 점수일 경우에는 가입날이 빠른사람이 노출됩니다
        // 겹지인으로 50명을 채우지 못하면 부족분을 랜덤 유저로 채우고, 친구가 없을 경우에도 랜덤 유저로 채웁니다
        // 모든 경우에서 최대 반환수는 50명입니다.
        // TODO: 배포후 상황에 따라서 페이지네이션으로 변경하거나, Redis 적용이 필요합니다

        // 내 친구 ID (탈퇴자 제외)
        List<Long> friendIds = friendRepository.findFriendIdsByUserId(myId);
        // 보냈거나 받은 친구 신청의 상대 ID — 양방향 모두 추천 제외 대상
        List<Long> requestPeerIds = friendRequestRepository.findRequestPeerIds(myId);

        // 차단했거나 차단당한 유저 ID — 추천에서 제외
        List<Long> blockedIds = userBlockRepository.findAllBlockRelatedUserIds(myId);

        // 친구 + 보냈거나 받은 친구 신청 + 차단 관계 유저 ID 제외
        Set<Long> excludeIds = new HashSet<>(friendIds);
        excludeIds.addAll(requestPeerIds);
        excludeIds.addAll(blockedIds);

        List<FriendResponse> result = new ArrayList<>();
        // 이미 추천 목록에 담은 유저 ID — 랜덤 보충 단계에서 중복으로 노출되지 않도록 추적
        Set<Long> addedIds = new HashSet<>();

        // 친구가 1명 이상이면 겹지인 우선으로 채움
        if (!friendIds.isEmpty()) {
            // 친구의 친구를 겹지인 수 내림차순, 동점이면 가입 순(id 오름차순)으로 조회
            List<RecommendedFriendProjection> candidates =
                    friendRepository.findRecommendationCandidates(friendIds, myId);

            // 제외 대상은 건너뛰고 최대 RECOMMENDATION_LIMIT명까지 결과에 담음
            for (RecommendedFriendProjection c : candidates) {
                if (excludeIds.contains(c.getId())) {
                    continue;
                }
                result.add(new FriendResponse(c.getHandle(), c.getUsername(), c.getProfileImageUrl()));
                addedIds.add(c.getId());
                if (result.size() >= RECOMMENDATION_LIMIT) {
                    break;
                }
            }
        }

        // 겹지인으로 50명을 못 채웠다면 랜덤 유저로 부족분 보충
        if (result.size() < RECOMMENDATION_LIMIT) {
            int remaining = RECOMMENDATION_LIMIT - result.size();
            // 기존 제외 셋에 이미 담은 추천 유저 ID까지 합쳐서 중복 노출 방지
            Set<Long> randomExcludes = new HashSet<>(excludeIds);
            randomExcludes.addAll(addedIds);
            result.addAll(findRandomRecommendations(myId, randomExcludes, remaining));
        }

        return result;
    }

    private List<FriendResponse> findRandomRecommendations(Long myId, Set<Long> excludeIds, int limit) {
        // 네이티브 쿼리의 NOT IN 절은 빈 리스트를 거부하므로, 비어 있으면 매칭되지 않는 sentinel(-1)로 채움
        List<Long> excludeForSql = excludeIds.isEmpty()
                ? List.of(-1L)
                : new ArrayList<>(excludeIds);

        // 자기 자신/탈퇴자/제외 대상을 빼고 랜덤으로 limit명 추출
        // TODO: RAND 함수 사용중이기에 나중에 규모가 커지면 다른 방법으로 교체가 필요함
        List<RecommendedFriendProjection> randomUsers =
                userRepository.findRandomActiveUsers(myId, excludeForSql, limit);

        List<FriendResponse> result = new ArrayList<>();
        for (RecommendedFriendProjection u : randomUsers) {
            result.add(new FriendResponse(u.getHandle(), u.getUsername(), u.getProfileImageUrl()));
        }
        return result;
    }

    public List<FriendResponse> getMutualFriends(Long myId, String handle) {
        User target = userRepository.findByHandleAndDeletedAtIsNull(handle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (myId.equals(target.getId())) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<FriendUserProjection> projections = friendRepository.findMutualFriends(myId, target.getId());

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
            if (userBlockRepository.existsBlockBetween(request.getSenderId(), request.getReceiverId())) {
                // TODO: 사용자에게 거절되었다는 메세지 전송 필요
                friendRequestRepository.delete(request);
                return;
            }

            Long userAId = Math.min(request.getSenderId(), request.getReceiverId());
            Long userBId = Math.max(request.getSenderId(), request.getReceiverId());
            friendRepository.save(Friend.builder().id(new FriendId(userAId, userBId)).build());

            eventPublisher.publishEvent(new FriendAcceptedEvent(
                    request.getSenderId(), receiverId));
        }

        friendRequestRepository.delete(request);
    }

    @Transactional
    public void deleteFriend(Long myId, String handle) {
        User target = userRepository.findByHandleAndDeletedAtIsNull(handle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!friendRepository.existsFriendship(myId, target.getId())) {
            throw new CustomException(ErrorCode.FRIEND_NOT_FOUND);
        }

        friendRepository.deleteFriendship(myId, target.getId());
    }

    @Transactional
    public void cancelRequest(Long senderId, String receiverHandle) {
        User receiver = userRepository.findByHandleAndDeletedAtIsNull(receiverHandle)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!friendRequestRepository.existsBySenderIdAndReceiverId(senderId, receiver.getId())) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }

        friendRequestRepository.deleteBySenderIdAndReceiverId(senderId, receiver.getId());
    }
}
