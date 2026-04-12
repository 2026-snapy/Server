package com.gbsw.snapy.domain.users.repository;

import com.gbsw.snapy.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByHandle(String handle);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);

    Optional<User> findByEmail(String email);
    Optional<User> findByHandle(String handle);
    List<User> findTop10ByPhoneIn(List<String> phones);
}