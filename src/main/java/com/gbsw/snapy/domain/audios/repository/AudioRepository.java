package com.gbsw.snapy.domain.audios.repository;

import com.gbsw.snapy.domain.audios.entity.Audio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AudioRepository extends JpaRepository<Audio, Long> {
}
