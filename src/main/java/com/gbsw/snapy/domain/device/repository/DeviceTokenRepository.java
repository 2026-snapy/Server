package com.gbsw.snapy.domain.device.repository;

import com.gbsw.snapy.domain.device.entity.DevicePlatform;
import com.gbsw.snapy.domain.device.entity.DeviceToken;
import com.gbsw.snapy.domain.device.entity.DeviceTokenEnvironment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {

    Optional<DeviceToken> findByToken(String token);

    List<DeviceToken> findByUserIdAndPlatformAndEnvironment(
            Long userId,
            DevicePlatform platform,
            DeviceTokenEnvironment environment
    );

    List<DeviceToken> findByUserIdAndPlatform(Long userId, DevicePlatform platform);

    void deleteByUserIdAndToken(Long userId, String token);
}
