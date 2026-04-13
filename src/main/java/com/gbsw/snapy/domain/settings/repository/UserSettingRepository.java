package com.gbsw.snapy.domain.settings.repository;

import com.gbsw.snapy.domain.settings.entity.UserSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserSettingRepository extends JpaRepository<UserSetting, Long> {
}
