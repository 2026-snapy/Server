package com.gbsw.snapy.domain.device.service;

import com.gbsw.snapy.domain.device.dto.request.DeviceTokenRegisterRequest;
import com.gbsw.snapy.domain.device.entity.DeviceToken;
import com.gbsw.snapy.domain.device.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;

    @Transactional
    public void register(Long userId, DeviceTokenRegisterRequest request) {
        deviceTokenRepository.findByToken(request.token())
                .ifPresentOrElse(
                        deviceToken -> deviceToken.update(userId, request.platform(), request.environment()),
                        () -> deviceTokenRepository.save(DeviceToken.builder()
                                .userId(userId)
                                .token(request.token())
                                .platform(request.platform())
                                .environment(request.environment())
                                .build())
                );
    }

    @Transactional
    public void delete(Long userId, String token) {
        deviceTokenRepository.deleteByUserIdAndToken(userId, token);
    }
}
