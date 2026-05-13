package com.gbsw.snapy.domain.phone.repository;

import com.gbsw.snapy.domain.phone.entity.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, Long> {

    Optional<PhoneVerification> findTopByPhoneOrderByCreatedAtDesc(String phone);

    @Modifying
    @Query("delete from PhoneVerification pv where pv.phone = :phone")
    void deleteAllByPhone(@Param("phone") String phone);
}
