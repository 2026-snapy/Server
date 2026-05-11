package com.gbsw.snapy.domain.device.service;

import com.gbsw.snapy.domain.device.dto.request.DeviceTokenRegisterRequest;
import com.gbsw.snapy.domain.device.entity.DeviceToken;
import com.gbsw.snapy.domain.device.repository.DeviceTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
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
                        () -> saveOrUpdateAfterConflict(userId, request)
                );
    }

    private void saveOrUpdateAfterConflict(Long userId, DeviceTokenRegisterRequest request) {
        try {
            deviceTokenRepository.saveAndFlush(DeviceToken.builder()
                    .userId(userId)
                    .token(request.token())
                    .platform(request.platform())
                    .environment(request.environment())
                    .build());
        } catch (DataIntegrityViolationException e) {
            DeviceToken deviceToken = deviceTokenRepository.findByToken(request.token())
                    .orElseThrow(() -> e);
            deviceToken.update(userId, request.platform(), request.environment());
        }
    }

    @Transactional
    public void delete(Long userId, String token) {
        deviceTokenRepository.deleteByUserIdAndToken(userId, token);
    }
}
