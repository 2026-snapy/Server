package com.gbsw.snapy.domain.phone.service;

import com.gbsw.snapy.domain.phone.entity.PhoneVerification;
import com.gbsw.snapy.domain.phone.repository.PhoneVerificationRepository;
import com.gbsw.snapy.domain.users.repository.UserRepository;
import com.gbsw.snapy.global.exception.CustomException;
import com.gbsw.snapy.global.exception.ErrorCode;
import com.gbsw.snapy.infra.aligo.AligoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PhoneVerificationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(5);
    private static final Duration RESEND_COOLDOWN = Duration.ofSeconds(90);
    private static final SecureRandom RANDOM = new SecureRandom();

    private final PhoneVerificationRepository phoneVerificationRepository;
    private final UserRepository userRepository;
    private final AligoClient aligoClient;

    // TODO: 추후 트래픽 증가시 Redis로 변경 필요
    @Transactional
    public void issueCode(String phone) {
//        LocalDateTime now = LocalDateTime.now();
//
//        if (userRepository.existsByPhone(phone)) {
//            throw new CustomException(ErrorCode.DUPLICATE_PHONE);
//        }
//
//        phoneVerificationRepository.findTopByPhoneOrderByCreatedAtDesc(phone)
//                .ifPresent(latest -> {
//                    if (latest.getCreatedAt().plus(RESEND_COOLDOWN).isAfter(now)) {
//                        throw new CustomException(ErrorCode.VERIFICATION_CODE_RESEND_COOLDOWN);
//                    }
//                });
//
//        String code = generateCode();
//        phoneVerificationRepository.deleteAllByPhone(phone);
//        phoneVerificationRepository.save(
//                PhoneVerification.builder()
//                        .phone(phone)
//                        .code(code)
//                        .expiresAt(now.plus(CODE_TTL))
//                        .createdAt(now)
//                        .build()
//        );
//
//        aligoClient.sendSms(phone, "[Snapy] 인증번호는 " + code + " 입니다" + " 5분안에 입력해주세요.");
    }

    @Transactional
    public void verifyAndConsume(String phone, String code) {
//        PhoneVerification verification = phoneVerificationRepository
//                .findTopByPhoneOrderByCreatedAtDesc(phone)
//                .orElseThrow(() -> new CustomException(ErrorCode.VERIFICATION_NOT_FOUND));
//
//        if (verification.isExpired(LocalDateTime.now())) {
//            throw new CustomException(ErrorCode.VERIFICATION_CODE_EXPIRED);
//        }
//        if (!verification.matches(code)) {
//            throw new CustomException(ErrorCode.INVALID_VERIFICATION_CODE);
//        }
//
//        phoneVerificationRepository.deleteAllByPhone(phone);
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
}
